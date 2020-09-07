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
package org.terasology.eventualSkills.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.gestalt.assets.ResourceUrn;

public class GiveSkillEvent implements Event {
    ResourceUrn skill;
    Integer level;

    public GiveSkillEvent() {
    }

    public GiveSkillEvent(String skill) {
        this.skill = new ResourceUrn(skill);
    }

    public GiveSkillEvent(ResourceUrn skill) {
        this.skill = skill;
    }

    public GiveSkillEvent(String skill, Integer level) {
        this(skill);
        this.level = level;
    }

    public GiveSkillEvent(ResourceUrn skill, Integer level) {
        this(skill);
        this.level = level;
    }

    public ResourceUrn getSkill() {
        return skill;
    }

    public Integer getLevel() {
        return level;
    }
}
