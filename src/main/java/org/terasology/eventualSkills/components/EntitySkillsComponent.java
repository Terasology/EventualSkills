/*
 * Copyright 2016 MovingBlocks
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

import com.google.common.collect.Maps;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.Map;

public class EntitySkillsComponent implements Component {

    // a map of the skill and the level to which it has been learned.  These must always be lowercased, otherwise chaos.
    @Replicate
    public Map<String, Integer> learnedSkills = Maps.newHashMap();

    public boolean hasSkill(ResourceUrn skillUrn, int level) {
        return getSkillLevel(skillUrn) >= level;
    }

    public int getSkillLevel(ResourceUrn skillUrn) {
        if (learnedSkills.containsKey(skillUrn.toString().toLowerCase())) {
            return learnedSkills.get(skillUrn.toString().toLowerCase());
        } else {
            return 0;
        }
    }
}
