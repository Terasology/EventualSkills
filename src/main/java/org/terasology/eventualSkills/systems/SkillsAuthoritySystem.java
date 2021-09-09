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
package org.terasology.eventualSkills.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.components.SkillGivingItemComponent;
import org.terasology.eventualSkills.events.GiveSkillEvent;
import org.terasology.eventualSkills.events.SkillTrainedEvent;
import org.terasology.eventualSkills.events.SkillTrainedOwnerEvent;
import org.terasology.gestalt.assets.ResourceUrn;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SkillsAuthoritySystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SkillsAuthoritySystem.class);

    @ReceiveEvent
    public void giveSkillToEntity(GiveSkillEvent event, EntityRef entityRef) {
        if (event.getSkill() != null && entityRef != null) {
            String skill = event.getSkill().toString().toLowerCase();
            EntitySkillsComponent skillComponent = getEntitySkillsComponent(entityRef);
            skillComponent.learnedSkills.put(skill, event.getLevel());
            entityRef.addOrSaveComponent(skillComponent);
            entityRef.send(new SkillTrainedEvent(event.getSkill(), event.getLevel()));
            logger.info(entityRef.toString() + " given skill " + skill + " level " + event.getLevel());
        }
    }

    @ReceiveEvent
    public void onSkillTrainedSendOwnerSkillTrainedEvent(SkillTrainedEvent event, EntityRef entityRef) {
        entityRef.send(new SkillTrainedOwnerEvent(event.getSkillTrained(), event.getLevelTrained()));
    }

    @ReceiveEvent
    public void onSkillGivingItemUsed(ActivateEvent event, EntityRef item, SkillGivingItemComponent skillGivingItemComponent) {
        EntitySkillsComponent skillsComponent = getEntitySkillsComponent(event.getInstigator());
        ResourceUrn skillUrn = new ResourceUrn(skillGivingItemComponent.skill);
        int currentLevel = skillsComponent == null ? 0 : skillsComponent.getSkillLevel(skillUrn);
        int level = skillGivingItemComponent.level != null ? Math.max(currentLevel, skillGivingItemComponent.level)
                : currentLevel + 1;

        event.getInstigator().send(new GiveSkillEvent(skillUrn, level));
    }

    private EntitySkillsComponent getEntitySkillsComponent(EntityRef entityRef) {
        EntitySkillsComponent entitySkillsComponent = entityRef.getComponent(EntitySkillsComponent.class);
        if (entitySkillsComponent == null) {
            entitySkillsComponent = new EntitySkillsComponent();
        }
        return entitySkillsComponent;
    }
}
