# 🚀 AWS Cloud Cost Optimization using Lambda (Java)

## 📌 Project Overview

This project automates AWS cloud cost optimization by identifying and deleting stale EBS snapshots using an AWS Lambda function written in Java.

The Lambda function:

- Fetches all EBS snapshots owned by the AWS account
- Checks whether the associated EBS volume exists
- Verifies whether the volume is attached to any EC2 instance
- Deletes stale snapshots that are no longer required
- Runs automatically every week using Amazon EventBridge (CloudWatch Scheduler)

This helps reduce unnecessary AWS storage costs and keeps the environment clean.

---

# 🛠️ Tech Stack

- Java 17
- AWS Lambda
- AWS SDK for Java v2
- Amazon EC2
- Amazon EventBridge / CloudWatch Scheduler
- Maven
- VS Code
- Git & GitHub

---

# 📂 Project Structure

```bash
aws-cost-optimization/
│
├── src/
│   └── main/
│       └── java/
│           └── com/example/awscostoptimisation/
│               └── DeleteStaleEbsSnapshot.java
│
├── pom.xml
├── README.md

⚙️ Features

✅ Automatically identifies stale EBS snapshots
✅ Deletes snapshots whose volumes no longer exist
✅ Deletes snapshots whose volumes are detached from EC2 instances
✅ Weekly automated execution using EventBridge Scheduler
✅ Serverless and cost-efficient architecture
✅ Fully built using Java and AWS SDK v2

🔍 Lambda Workflow
The Lambda function performs the following operations:

Fetches all EBS snapshots owned by the AWS account
1) Checks whether the associated EBS volume exists
2) Validates whether the volume is attached to an EC2 instance
3) Verifies whether the EC2 instance is in running state
4) Deletes stale snapshots if:
   . Volume no longer exists
   . Volume is unattached
   . Volume is not attached to a running EC2 instance

☁️ AWS Services Used
Service	Purpose
AWS Lambda	Executes cleanup logic
Amazon EC2	Fetches snapshots and volumes
CloudWatch Logs	Monitoring and debugging
EventBridge Scheduler	Weekly Lambda trigger
IAM        Permissions management

🔐 IAM Permissions Required
The Lambda execution role requires permissions for:
ec2:DescribeSnapshots
ec2:DescribeVolumes
ec2:DescribeInstances
ec2:DeleteSnapshot
logs:CreateLogGroup
logs:CreateLogStream
logs:PutLogEvents

⏰ Scheduler Configuration
The Lambda function is configured to run automatically every Monday at 11:00 AM using Amazon EventBridge Scheduler.

Cron Expression:
cron(0 11 ? * MON *)

🚀 Deployment Steps
1️⃣ Build the Project
mvn clean package

Generated JAR file:
target/aws-cost-optimisation-1.0-SNAPSHOT.jar

2️⃣ Create AWS Lambda Function
Runtime: Java 17
Architecture: x86_64
Upload the generated JAR file

Handler:
com.example.awscostoptimisation.DeleteStaleEbsSnapshot::handleRequest

3️⃣ Attach IAM Permissions
Attach EC2 and CloudWatch permissions to the Lambda execution role.

4️⃣ Configure EventBridge Scheduler
Create a scheduled rule to trigger Lambda weekly.

💡 Benefits
Reduces unnecessary AWS storage costs
Automates infrastructure cleanup
Improves cloud resource management
Demonstrates serverless automation skills
Production-style cloud optimization use case

🎯 Learning Outcomes

Through this project, I gained hands-on experience with:

AWS Lambda
Java-based serverless development
AWS SDK v2
EC2 snapshot automation
IAM policy configuration
EventBridge scheduling
CloudWatch monitoring
Maven project management
Cloud cost optimization practices

👩‍💻 Author
Vanshika Jaiswal
```
