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
package org.terasology.eventualSkills.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.events.GiveSkillEvent;
import org.terasology.eventualSkills.events.SkillTrainedEvent;
import org.terasology.eventualSkills.events.StartTrainingSkillRequestEvent;
import org.terasology.eventualSkills.events.StopTrainingSkillRequestEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

import java.util.Map;
import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class EventualSkillAuthoritySystem extends BaseComponentSystem {
    static final double SKILL_POINTS_PER_MILLISECOND = 37.5 / 60.0 / 1000.0; // aka 37.5 per minute
    private static final Logger logger = LoggerFactory.getLogger(EventualSkillAuthoritySystem.class);
    private static final String DELAY_MANAGER_ACTION = "EventualSkills:TrainingComplete";

    @In
    Time time;
    @In
    DelayManager delayManager;
    @In
    EventualSkillsManager eventualSkillsManager;

    @ReceiveEvent
    public void rescheduleSkillAfterSkillTrained(SkillTrainedEvent event, EntityRef entityRef, EntityEventualSkillsComponent entityEventualSkillsComponent) {
        if (event.getSkillTrained().toString().equals(entityEventualSkillsComponent.currentSkillInTraining)) {
            stopTraining(entityRef);
            entityEventualSkillsComponent.partiallyLearnedSkills.remove(entityEventualSkillsComponent.currentSkillInTraining);
            entityRef.saveComponent(entityEventualSkillsComponent);
        }
    }

    @ReceiveEvent
    public void onTrainingDelayCompletion(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(DELAY_MANAGER_ACTION)) {
            completeTraining(entityRef);
        }
    }

    @ReceiveEvent
    public void onRequestStartTraining(StartTrainingSkillRequestEvent event, EntityRef entity) {
        startTraining(entity, new ResourceUrn(event.skill));
    }

    @ReceiveEvent
    public void onRequestStopTraining(StopTrainingSkillRequestEvent event, EntityRef entity) {
        stopTraining(entity);
    }

    @Command(shortDescription = "Finish training the skill currently being trained", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String finishTrainingEventualSkill(
            @Sender EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        EntityEventualSkillsComponent entityEventualSkillsComponent = playerEntity.getComponent(EntityEventualSkillsComponent.class);
        String currentSkillInTraining = entityEventualSkillsComponent.currentSkillInTraining;
        if (currentSkillInTraining != null) {
            completeTraining(playerEntity);
            return currentSkillInTraining + " completed";
        } else {
            return "No skill currently being trained";
        }
    }

    private void startTraining(EntityRef entityRef, ResourceUrn skillUrn) {
        if (skillUrn != null && entityRef != null) {
            EntityEventualSkillsComponent eventualSkillsComponent = getEntityEventualSkillsComponent(entityRef);
            EntitySkillsComponent skillsComponent = getEntitySkillsComponent(entityRef);
            if (eventualSkillsManager.getPrerequisiteSkillsNeeded(skillsComponent, eventualSkillsComponent, skillUrn).size() == 0) {
                savePartialTraining(skillsComponent, eventualSkillsComponent);
                setSkillInTraining(skillsComponent, eventualSkillsComponent, skillUrn);
                entityRef.addOrSaveComponent(eventualSkillsComponent);
                scheduleEntityTrainingCompletion(eventualSkillsComponent, entityRef);
                logger.info(entityRef.toString() + " started training skill " + skillUrn.toString());
            } else {
                logger.info(entityRef.toString() + " does not have the prerequisites to train the skill " + skillUrn.toString());
            }
        }
    }

    private void stopTraining(EntityRef entityRef) {
        if (entityRef != null) {
            EntityEventualSkillsComponent eventualSkillsComponent = getEntityEventualSkillsComponent(entityRef);
            EntitySkillsComponent skillsComponent = getEntitySkillsComponent(entityRef);
            savePartialTraining(skillsComponent, eventualSkillsComponent);
            setSkillInTraining(skillsComponent, eventualSkillsComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
            entityRef.addOrSaveComponent(eventualSkillsComponent);
            scheduleEntityTrainingCompletion(eventualSkillsComponent, entityRef);
            logger.info(entityRef.toString() + " stopped training skill");
        }
    }

    private void completeTraining(EntityRef entityRef) {
        if (entityRef != null) {
            EntityEventualSkillsComponent eventualSkillsComponent = getEntityEventualSkillsComponent(entityRef);
            String completedSkill = eventualSkillsComponent.currentSkillInTraining;
            if (completedSkill != null) {
                if (eventualSkillsComponent.currentSkillInTraining != null) {
                    entityRef.send(new GiveSkillEvent(eventualSkillsComponent.currentSkillInTraining, eventualSkillsComponent.currentSkillLevelInTraining));
                    logger.info(entityRef.toString() + " completed training skill " + completedSkill);
                }
            }
        }
    }
    private void scheduleEntityTrainingCompletion(EntityEventualSkillsComponent skillComponent, EntityRef entityRef) {
        if (delayManager.hasDelayedAction(entityRef, DELAY_MANAGER_ACTION)) {
            delayManager.cancelDelayedAction(entityRef, DELAY_MANAGER_ACTION);
        }
        long delayAmount = (long) ((skillComponent.currentTrainingTargetSkillPoints - skillComponent.currentTrainingCurrentSkillPoints) / SKILL_POINTS_PER_MILLISECOND);
        if (delayAmount <= 0) {
            completeTraining(entityRef);
            return;
        }
        delayManager.addDelayedAction(entityRef, DELAY_MANAGER_ACTION, delayAmount);
    }

    private void savePartialTraining(EntitySkillsComponent skillsComponent, EntityEventualSkillsComponent entityEventualSkillsComponent) {
        if (entityEventualSkillsComponent.currentSkillInTraining != null) {
            ResourceUrn currentSkillInTrainingUrn = new ResourceUrn(entityEventualSkillsComponent.currentSkillInTraining);
            // only save the partial skill training if we have not already learned this skill level
            if (entityEventualSkillsComponent.currentSkillLevelInTraining > skillsComponent.getSkillLevel(currentSkillInTrainingUrn)) {
                calculatePartialTraining(entityEventualSkillsComponent);
                entityEventualSkillsComponent.partiallyLearnedSkills.put(entityEventualSkillsComponent.currentSkillInTraining.toLowerCase(), entityEventualSkillsComponent.currentTrainingCurrentSkillPoints);
            } else {
                entityEventualSkillsComponent.partiallyLearnedSkills.remove(entityEventualSkillsComponent.currentSkillInTraining.toLowerCase());
            }
        }
        entityEventualSkillsComponent.currentSkillInTraining = null;
        entityEventualSkillsComponent.currentTrainingCurrentSkillPoints = 0;
        entityEventualSkillsComponent.currentSkillLevelInTraining = 0;
        entityEventualSkillsComponent.currentSkillRankInTraining = 0;
        entityEventualSkillsComponent.trainingLastTimeComputedSkillPoints = Long.MAX_VALUE;
    }

    private void calculatePartialTraining(EntityEventualSkillsComponent skillComponent) {
        long currentTime = time.getGameTimeInMs();
        long lastComputedTime = skillComponent.trainingLastTimeComputedSkillPoints;
        int newSkillPoints = (int) ((double) (currentTime - lastComputedTime) * SKILL_POINTS_PER_MILLISECOND);
        skillComponent.currentTrainingCurrentSkillPoints += newSkillPoints;
        skillComponent.trainingLastTimeComputedSkillPoints = currentTime;
    }

    private void setSkillInTraining(EntitySkillsComponent skillsComponent, EntityEventualSkillsComponent eventualSkillsComponent, ResourceUrn skillUrn) {
        String skill = skillUrn.toString().toLowerCase();
        Optional<Prefab> skillPrefab = Assets.getPrefab(skill);
        if (!skillPrefab.isPresent() || !skillPrefab.get().hasComponent(EventualSkillDescriptionComponent.class)) {
            logger.warn("Skill, " + skill + " is not valid");
            return;
        }
        EventualSkillDescriptionComponent skillDescription = skillPrefab.get().getComponent(EventualSkillDescriptionComponent.class);

        // validate that the skill to be trained has all the prerequisiteSkills met
        for (Map.Entry<String, Integer> skillReq : skillDescription.prerequisiteSkills.entrySet()) {
            if (!skillsComponent.hasSkill(new ResourceUrn(skillReq.getKey()), skillReq.getValue())) {
                logger.warn("Cannot train skill, " + skill + " the prerequisite has not been trained, " + skillReq.getKey() + " level " + skillReq.getValue());
                if (skill.equals(EventualSkillsCommonSystem.IDLE_SKILL_URN.toString())) {
                    // avoid stack overflow
                    return;
                }
                setSkillInTraining(skillsComponent, eventualSkillsComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
                return;
            }
        }

        // load any partial training
        if (eventualSkillsComponent.partiallyLearnedSkills.containsKey(skill)) {
            eventualSkillsComponent.currentTrainingCurrentSkillPoints = eventualSkillsComponent.partiallyLearnedSkills.get(skill);
        } else {
            eventualSkillsComponent.currentTrainingCurrentSkillPoints = 0;
        }

        // get the next level to train
        eventualSkillsComponent.currentSkillLevelInTraining = 1;
        if (skillsComponent.learnedSkills.containsKey(skill)) {
            eventualSkillsComponent.currentSkillLevelInTraining = skillsComponent.learnedSkills.get(skill) + 1;
        }

        eventualSkillsComponent.currentSkillRankInTraining = skillDescription.rank;
        eventualSkillsComponent.currentTrainingTargetSkillPoints =
                eventualSkillsManager.skillPointsNeeded(eventualSkillsComponent.currentSkillRankInTraining, eventualSkillsComponent.currentSkillLevelInTraining);
        eventualSkillsComponent.currentSkillInTraining = skill;
        eventualSkillsComponent.trainingLastTimeComputedSkillPoints = time.getGameTimeInMs();
    }


    private EntityEventualSkillsComponent getEntityEventualSkillsComponent(EntityRef entityRef) {
        EntityEventualSkillsComponent entityEventualSkillsComponent = entityRef.getComponent(EntityEventualSkillsComponent.class);
        if (entityEventualSkillsComponent == null) {
            entityEventualSkillsComponent = new EntityEventualSkillsComponent();
        }
        return entityEventualSkillsComponent;
    }

    private EntitySkillsComponent getEntitySkillsComponent(EntityRef entityRef) {
        EntitySkillsComponent entitySkillsComponent = entityRef.getComponent(EntitySkillsComponent.class);
        if (entitySkillsComponent == null) {
            entitySkillsComponent = new EntitySkillsComponent();
        }
        return entitySkillsComponent;
    }
}
