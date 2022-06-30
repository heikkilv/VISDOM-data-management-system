// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas


final case class ModuleAverageSchema(
    module_number: Int,
    grade: Int,
    total: Int,
    avg_points: Double,
    avg_exercises: Double,
    avg_submissions: Double,
    avg_commits: Double
) {
    def sum(other: ModuleAverageSchema): ModuleAverageSchema = {
        // assumes that module_number, grade, and total are equal in both schemas
        ModuleAverageSchema(
            module_number = module_number,
            grade = grade,
            total = total,
            avg_points = avg_points + other.avg_points,
            avg_exercises = avg_exercises + other.avg_exercises,
            avg_submissions = avg_submissions + other.avg_submissions,
            avg_commits = avg_commits + other.avg_commits
        )
    }
}

object ModuleAverageSchema {
    def getEmpty(moduleNumber: Int, grade: Int): ModuleAverageSchema = {
        ModuleAverageSchema(
            module_number = moduleNumber,
            grade = grade,
            total = 0,
            avg_points = 0.0,
            avg_exercises = 0.0,
            avg_submissions = 0.0,
            avg_commits = 0.0
        )
    }
}
