output "alerts_topic_arn" {
  description = "SNS topic ARN used for CloudWatch and Budget notifications."
  value       = aws_sns_topic.alerts.arn
}

output "monthly_budget_name" {
  description = "AWS Budget resource name."
  value       = aws_budgets_budget.monthly_cost.name
}

output "autoscaling_group_name" {
  description = "Auto Scaling Group name."
  value       = aws_autoscaling_group.app.name
}

output "high_cpu_alarm_name" {
  description = "CloudWatch CPU alarm name."
  value       = aws_cloudwatch_metric_alarm.high_cpu.alarm_name
}

output "high_memory_alarm_name" {
  description = "CloudWatch memory alarm name when enabled."
  value       = try(aws_cloudwatch_metric_alarm.high_memory[0].alarm_name, null)
}
