rootProject.name = "LearningManagementSystem"

plugins {
  id("org.sonarqube") version "7.5.0.8588"
}

sonar {
  properties {
    property("sonar.projectKey", "TechWizard-SS_hexlet-jdbc")
    property("sonar.organization", "techwizard-ss")
  }
}
