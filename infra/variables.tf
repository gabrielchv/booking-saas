variable "project_id" {
  description = "GCP project id"
  type        = string
}

variable "region" {
  description = "GCP region for Cloud Run, Cloud SQL and Artifact Registry"
  type        = string
  default     = "southamerica-east1"
}

variable "service_name" {
  description = "Cloud Run service name"
  type        = string
  default     = "booking-api"
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "booking"
}

variable "db_user" {
  description = "Database user"
  type        = string
  default     = "booking"
}

variable "database_password" {
  description = "Cloud SQL password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret, at least 32 bytes"
  type        = string
  sensitive   = true
}

variable "github_repo" {
  description = "GitHub repository in owner/repo form (for Workload Identity Federation)"
  type        = string
}
