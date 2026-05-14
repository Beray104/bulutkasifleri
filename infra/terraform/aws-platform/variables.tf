variable "aws_region" {
  description = "AWS region where monitoring, budgets, and compute resources are created."
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Project name used in AWS resource names."
  type        = string
  default     = "sosyal-medya-analiz"
}

variable "environment" {
  description = "Deployment environment label."
  type        = string
  default     = "dev"
}

variable "notification_email" {
  description = "Email address that receives budget and alarm notifications."
  type        = string
}

variable "monthly_budget_usd" {
  description = "Monthly AWS budget limit in USD."
  type        = number
  default     = 100
}

variable "vpc_id" {
  description = "VPC ID where the Auto Scaling Group instances run."
  type        = string
}

variable "subnet_ids" {
  description = "Subnet IDs used by the Auto Scaling Group."
  type        = list(string)
}

variable "ami_id" {
  description = "AMI ID used by EC2 instances in the launch template."
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type for application workers."
  type        = string
  default     = "t3.micro"
}

variable "key_name" {
  description = "Optional EC2 key pair name for SSH access."
  type        = string
  default     = null
}

variable "allowed_ssh_cidr_blocks" {
  description = "CIDR blocks allowed to reach EC2 instances through SSH."
  type        = list(string)
  default     = []
}

variable "asg_min_size" {
  description = "Minimum number of EC2 instances in the Auto Scaling Group."
  type        = number
  default     = 1
}

variable "asg_desired_capacity" {
  description = "Desired number of EC2 instances in the Auto Scaling Group."
  type        = number
  default     = 2
}

variable "asg_max_size" {
  description = "Maximum number of EC2 instances in the Auto Scaling Group."
  type        = number
  default     = 4
}

variable "cpu_alarm_threshold" {
  description = "CPU percentage that triggers the CloudWatch alarm."
  type        = number
  default     = 80
}

variable "enable_memory_alarm" {
  description = "Creates a RAM alarm based on the CloudWatch Agent mem_used_percent metric."
  type        = bool
  default     = true
}

variable "memory_alarm_threshold" {
  description = "Memory percentage that triggers the CloudWatch alarm."
  type        = number
  default     = 80
}

variable "target_cpu_utilization" {
  description = "Average ASG CPU target used by target tracking scaling."
  type        = number
  default     = 60
}

variable "user_data_base64" {
  description = "Optional base64-encoded EC2 user data script."
  type        = string
  default     = null
}
