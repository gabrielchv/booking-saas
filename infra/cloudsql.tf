resource "google_sql_database_instance" "db" {
  name             = "booking-db"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier              = "db-f1-micro"
    availability_type = "ZONAL"

    ip_configuration {
      ipv4_enabled = false
    }
  }

  deletion_protection = false

  depends_on = [google_project_service.required]
}

resource "google_sql_database" "database" {
  name     = var.db_name
  instance = google_sql_database_instance.db.name
}

resource "google_sql_user" "user" {
  name     = var.db_user
  instance = google_sql_database_instance.db.name
  password = var.database_password
}
