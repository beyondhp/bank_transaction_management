#!/usr/bin/env python3
"""
Transaction Generator Script

This script generates 100 random bank transactions by calling the transaction creation API.
It's useful for quickly generating test data for the Bank Transaction Management System.
"""

import requests
import random
import json
import uuid
import time
from datetime import datetime, timedelta

# Configuration
API_BASE_URL = "http://localhost:8080/api"
TRANSACTION_ENDPOINT = "/transactions"
NUM_TRANSACTIONS = 100

# Sample account numbers
SOURCE_ACCOUNTS = [
    "ACCT12345678",
    "ACCT23456789",
    "ACCT34567890",
    "ACCT45678901",
    "ACCT56789012"
]

DESTINATION_ACCOUNTS = [
    "ACCT87654321",
    "ACCT98765432",
    "ACCT09876543",
    "ACCT10987654",
    "ACCT21098765"
]

# Sample descriptions
DEPOSIT_DESCRIPTIONS = [
    "Salary payment",
    "Interest earned",
    "Refund received",
    "Tax return",
    "Dividend payment"
]

WITHDRAWAL_DESCRIPTIONS = [
    "ATM withdrawal",
    "Bill payment",
    "Loan payment",
    "Subscription fee",
    "Service charge"
]

TRANSFER_DESCRIPTIONS = [
    "Transfer to savings",
    "Payment for services",
    "Rent payment",
    "Utility payment",
    "Insurance premium"
]

# Transaction types and statuses
TRANSACTION_TYPES = ["DEPOSIT", "WITHDRAWAL", "TRANSFER"]
TRANSACTION_STATUSES = [
    "INITIATED",
    "PENDING",
    "PROCESSING",
    "COMPLETED",
    "FAILED",
    "REJECTED",
    "CANCELLED",
    "ON_HOLD",
    "SCHEDULED"
]

# Status weights (to make COMPLETED and INITIATED more common)
STATUS_WEIGHTS = {
    "INITIATED": 30,
    "COMPLETED": 45,
    "PENDING": 15,
    "PROCESSING": 5,
    "FAILED": 2,
    "REJECTED": 1,
    "CANCELLED": 1,
    "ON_HOLD": 1,
    "SCHEDULED": 0  # 设为0使其不会被选中
}


def generate_random_timestamp():
    """Generate a random timestamp within the last 30 days"""
    now = datetime.now()
    random_days = random.randint(0, 30)
    random_hours = random.randint(0, 23)
    random_minutes = random.randint(0, 59)
    random_seconds = random.randint(0, 59)
    
    random_time = now - timedelta(
        days=random_days,
        hours=random_hours,
        minutes=random_minutes,
        seconds=random_seconds
    )
    
    return random_time.isoformat()


def generate_random_amount():
    """Generate a random transaction amount between $10 and $10,000"""
    amount = round(random.uniform(10, 10000), 2)
    return amount


def get_random_status():
    """Get a random status based on the defined weights"""
    statuses = []
    weights = []
    
    for status, weight in STATUS_WEIGHTS.items():
        statuses.append(status)
        weights.append(weight)
    
    return random.choices(statuses, weights=weights, k=1)[0]


def create_random_transaction():
    """Create a random transaction object"""
    transaction_type = random.choice(TRANSACTION_TYPES)
    
    transaction = {
        "description": "",
        "amount": generate_random_amount(),
        "type": transaction_type,
        "status": get_random_status(),
        "timestamp": generate_random_timestamp(),
        "bankReference": f"REF{uuid.uuid4().hex[:8].upper()}"
    }
    
    # Set source and destination accounts based on transaction type
    if transaction_type == "DEPOSIT":
        transaction["sourceAccount"] = None
        transaction["destinationAccount"] = random.choice(DESTINATION_ACCOUNTS)
        transaction["description"] = random.choice(DEPOSIT_DESCRIPTIONS)
    elif transaction_type == "WITHDRAWAL":
        transaction["sourceAccount"] = random.choice(SOURCE_ACCOUNTS)
        transaction["destinationAccount"] = None
        transaction["description"] = random.choice(WITHDRAWAL_DESCRIPTIONS)
    else:  # TRANSFER
        transaction["sourceAccount"] = random.choice(SOURCE_ACCOUNTS)
        transaction["destinationAccount"] = random.choice(DESTINATION_ACCOUNTS)
        transaction["description"] = random.choice(TRANSFER_DESCRIPTIONS)
    
    return transaction


def send_transaction(transaction):
    """Send a transaction to the API"""
    url = f"{API_BASE_URL}{TRANSACTION_ENDPOINT}"
    headers = {
        "Content-Type": "application/json"
    }
    
    try:
        response = requests.post(url, headers=headers, data=json.dumps(transaction))
        response.raise_for_status()
        return True, response.json()
    except requests.exceptions.RequestException as e:
        return False, str(e)


def main():
    """Main function to generate and send transactions"""
    print(f"Starting to generate {NUM_TRANSACTIONS} transactions...")
    
    success_count = 0
    error_count = 0
    type_counts = {"DEPOSIT": 0, "WITHDRAWAL": 0, "TRANSFER": 0}
    
    start_time = time.time()
    
    for i in range(NUM_TRANSACTIONS):
        transaction = create_random_transaction()
        success, result = send_transaction(transaction)
        
        if success:
            success_count += 1
            type_counts[transaction["type"]] += 1
            print(f"[{i+1}/{NUM_TRANSACTIONS}] Created {transaction['type']} transaction: ID {result.get('id', 'N/A')}")
        else:
            error_count += 1
            print(f"[{i+1}/{NUM_TRANSACTIONS}] Error creating transaction: {result}")
        
        # Small delay to avoid overwhelming the API
        time.sleep(0.1)
    
    end_time = time.time()
    total_time = end_time - start_time
    
    print("\nTransaction Generation Summary:")
    print(f"Total time: {total_time:.2f} seconds")
    print(f"Successful: {success_count}/{NUM_TRANSACTIONS}")
    print(f"Failed: {error_count}/{NUM_TRANSACTIONS}")
    print("\nTransaction Types:")
    for t_type, count in type_counts.items():
        print(f"  {t_type}: {count}")


if __name__ == "__main__":
    main() 