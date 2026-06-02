#!/usr/bin/env python3
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone


BASE = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:8091"


def request(method, path, payload=None, expected=(200,)):
    data = None
    headers = {"Accept": "application/json"}
    if payload is not None:
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(BASE + path, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            body = resp.read().decode("utf-8")
            if resp.status not in expected:
                raise AssertionError(f"{method} {path} expected {expected}, got {resp.status}: {body}")
            return json.loads(body) if body else None
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8")
        raise AssertionError(f"{method} {path} failed {exc.code}: {body}") from exc


def wait_for_service():
    deadline = time.time() + 60
    while time.time() < deadline:
        try:
            request("GET", "/api/product-bundles?size=1")
            return
        except Exception:
            time.sleep(1)
    raise RuntimeError("Service did not become ready")


def iso_now():
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def main():
    wait_for_service()
    request("POST", "/api/admin/cleanup")

    init = request("POST", "/api/admin/product-bundles/initialize")
    assert init["parsedLeafCategories"] > 0, init

    bundles = request("GET", "/api/product-bundles?size=1")
    assert bundles["totalElements"] > 0, bundles
    bundle = bundles["content"][0]
    by_code = request("GET", f"/api/product-bundles/by-code/{urllib.parse.quote(bundle['code'])}")
    assert by_code["id"] == bundle["id"]

    lead_payload = {
        "leadNo": "LD-E2E-0001",
        "customerEmail": "customer@example.com",
        "customerEmailNormalized": "customer@example.com",
        "customerName": "Daniel Villarreal",
        "company": "Bionova Scientific",
        "productBundleId": bundle["id"],
        "ownerSalesEmail": "sales@example.com",
        "status": "DISCOVERY",
        "intentLevel": "HIGH",
        "source": "EMAIL",
        "inquirySummary": "Customer asks about a product bundle",
        "extractedRequirements": {"proteinCount": 3},
    }
    lead = request("POST", "/api/leads", lead_payload)
    assert lead["leadNo"] == "LD-E2E-0001"
    lead_again = request("POST", "/api/leads", {**lead_payload, "company": "Bionova Scientific LLC"})
    assert lead_again["id"] == lead["id"]
    assert lead_again["company"] == "Bionova Scientific LLC"

    email_payload = {
        "providerEmailId": "EMAIL-E2E-0001",
        "mailbox": "sales@example.com",
        "threadId": "THREAD-E2E-0001",
        "direction": "INBOUND_CUSTOMER",
        "fromEmail": "customer@example.com",
        "fromName": "Daniel",
        "toEmails": ["sales@example.com"],
        "ccEmails": [],
        "subject": "Product inquiry",
        "bodyText": "Hello, please send details.",
        "snippet": "Hello, please send details.",
        "receivedAt": iso_now(),
    }
    email = request("POST", "/api/emails", email_payload)
    assert email["providerEmailId"] == "EMAIL-E2E-0001"

    lead_email = request("POST", "/api/lead-emails", {
        "leadId": lead["id"],
        "emailMessageId": email["id"],
        "relationType": "FIRST_INQUIRY",
        "matchConfidence": 0.99,
        "matchReason": "Agent selected explicit relation",
    })
    assert lead_email["leadId"] == lead["id"]
    lead_emails = request("GET", f"/api/leads/{lead['id']}/emails")
    assert len(lead_emails) == 1

    task = request("POST", "/api/follow-up-tasks", {
        "taskNo": "TASK-E2E-0001",
        "leadId": lead["id"],
        "sourceEmailId": email["id"],
        "assignedSalesEmail": "sales@example.com",
        "status": "PROPOSED",
        "taskType": "CLARIFY_REQUIREMENTS",
        "priority": "HIGH",
        "title": "Clarify requirements",
        "summary": "Ask customer for details",
        "displaySummary": "Ask for sequence and target yield",
        "actionItems": ["Ask sequence", "Ask target yield"],
    })
    assert task["status"] == "PROPOSED"

    task_updated = request("PUT", f"/api/follow-up-tasks/{task['id']}", {
        **{k: task[k] for k in [
            "taskNo", "leadId", "sourceEmailId", "assignedSalesEmail", "taskType", "priority",
            "title", "summary", "displaySummary", "actionItems"
        ]},
        "status": "DONE",
        "closeReason": "Agent marked done",
    })
    assert task_updated["status"] == "DONE"

    activity = request("POST", "/api/sales-activities", {
        "activityNo": "ACT-E2E-0001",
        "leadId": lead["id"],
        "sourceEmailId": email["id"],
        "relatedTaskId": task["id"],
        "salesEmail": "sales@example.com",
        "activityType": "EMAIL_SENT",
        "occurredAt": iso_now(),
        "title": "Sales replied",
        "summary": "Sales replied to customer",
        "keyPoints": ["Sent product information"],
        "customerSignals": ["Interested"],
        "nextStepSignals": ["Wait for confirmation"],
        "progressSignal": "PROGRESS",
        "extractedPayload": {"source": "agent"},
        "confidence": 0.98,
    })
    assert activity["activityNo"] == "ACT-E2E-0001"
    activities = request("GET", f"/api/leads/{lead['id']}/activities")
    assert len(activities) == 1

    summary = request("POST", "/api/lead-activity-summaries", {
        "leadId": lead["id"],
        "summaryWindow": "LAST_30_DAYS",
        "windowStartAt": "2026-05-01T00:00:00Z",
        "windowEndAt": "2026-05-30T00:00:00Z",
        "overallSummary": "Lead has a stored activity summary",
        "customerIntent": "YES",
        "currentStage": "ENGAGED",
        "trend": "IMPROVING",
        "progressActivityCount": 1,
        "noProgressActivityCount": 0,
        "sourceActivityIds": [activity["id"]],
        "confidence": 0.96,
    })
    assert summary["trend"] == "IMPROVING"
    latest_summary = request("GET", f"/api/leads/{lead['id']}/activity-summary?summaryWindow=LAST_30_DAYS")
    assert latest_summary["id"] == summary["id"]

    timeline = request("GET", f"/api/leads/{lead['id']}/timeline")
    assert timeline["leadId"] == lead["id"]
    types = {item["type"] for item in timeline["items"]}
    assert {"EMAIL", "FOLLOW_UP_TASK", "SALES_ACTIVITY"}.issubset(types), timeline

    request("POST", "/api/admin/cleanup")
    empty = request("GET", "/api/leads?size=1")
    assert empty["totalElements"] == 0
    print(json.dumps({"status": "success", "initialized": init["parsedLeafCategories"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
