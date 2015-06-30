/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.eventualSkills.components;

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.Component;

import java.util.HashMap;
import java.util.Map;

public class EntityEventualSkillsComponent implements Component {
    public String currentSkillInTraining;
    public int currentSkillLevelInTraining;
    public int currentSkillRankInTraining;
    public int currentTrainingTargetSkillPoints;
    public int currentTrainingCurrentSkillPoints;
    public long trainingLastTimeComputedSkillPoints;
    // a map of the skill and the level to which it has been learned
    public Map<String, Integer> learnedSkills = new HashMap<>();
    // a map of the skill and how many skill points have already been acquired
    public Map<String, Integer> partiallyLearnedSkills = new HashMap<>();

    public boolean hasSkill(ResourceUrn skillUrn, int level) {
        return learnedSkills.containsKey(skillUrn.toString())
                && learnedSkills.get(skillUrn.toString()) >= level;
    }

    public int getSkillLevel(ResourceUrn skillUrn) {
        if (learnedSkills.containsKey(skillUrn.toString())) {
            return learnedSkills.get(skillUrn.toString());
        } else {
            return 0;
        }
    }
}
