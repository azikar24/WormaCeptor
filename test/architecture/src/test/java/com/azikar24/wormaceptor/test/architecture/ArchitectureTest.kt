package com.azikar24.wormaceptor.test.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.Test

class ArchitectureTest {
    @Test
    fun domainShouldNotDependOnAndroidTypes() {
        val importedClasses = ClassFileImporter().importPackages("com.azikar24.wormaceptor.domain")

        // Domain entities should be framework agnostic
        val rule = noClasses().that().resideInAPackage("..domain.entities..")
            .should().dependOnClassesThat().resideInAPackage("android.widget..")
            .orShould().dependOnClassesThat().resideInAPackage("android.app..")
            .orShould().dependOnClassesThat().resideInAPackage("android.view..")

        rule.check(importedClasses)
    }
}
