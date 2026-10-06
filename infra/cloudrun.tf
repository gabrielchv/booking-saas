data "google_project" "project" {}

resource "google_cloud_run_v2_service" "api" {
  name     = var.service_name
  location = var.region

  template {
    service_account = google_service_account.deployer.email

    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [google_sql_database_instance.db.connection_name]
      }
    }

    containers {
      image = "${google_artifact_registry_repository.repo.location}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.repo.repository_id}/booking-api:latest"

      volume_mounts {
        name       = "cloudsql"
        mount_path = "/cloudsql"
      }

      env {
        name  = "DB_URL"
        value = "jdbc:postgresql:///${var.db_name}?cloudSqlInstance=${google_sql_database_instance.db.connection_name}&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
      }
      env {
        name  = "DB_USERNAME"
        value = var.db_user
      }
      env {
        name  = "DB_PASSWORD"
        value = var.database_password
      }
      env {
        name  = "JWT_SECRET"
        value = var.jwt_secret
      }
    }

    scaling {
      min_instance_count = 0
      max_instance_count = 2
    }
  }

  ingress = "INGRESS_TRAFFIC_ALL"

  depends_on = [google_project_service.required]
}
