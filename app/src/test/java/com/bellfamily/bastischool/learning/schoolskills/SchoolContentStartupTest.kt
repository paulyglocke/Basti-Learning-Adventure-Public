package com.bellfamily.bastischool.learning.schoolskills

import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolContentStartupTest {
    @Test fun allRepositoriesUsedDuringAppStartupInitializeSuccessfully() {
        // Accessing these objects reproduces the v1019501 initializer failure before packaging.
        val repositories = listOf(SchoolSkillsContent.repository, SchoolActionsContent.repository,
            SchoolActionSequenceContent.repository, SchoolActionTripleContent.repository, ClassroomHelperContent.repository)
        repositories.forEach {
            assertTrue(it.version.schema > 0 && it.version.revision > 0)
            assertTrue(it.all().isNotEmpty())
        }
    }
}
