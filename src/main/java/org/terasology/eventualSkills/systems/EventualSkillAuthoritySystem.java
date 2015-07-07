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
import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.events.RequestStartTraining;
import org.terasology.eventualSkills.events.RequestStopTraining;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

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

    public void giveSkill(EntityRef entityRef, ResourceUrn skillUrn, int level) {
        if (skillUrn != null && entityRef != null) {
            String skill = skillUrn.toString();
            EntityEventualSkillsComponent skillComponent = getEntityEventualSkillComponent(entityRef);
            skillComponent.partiallyLearnedSkills.remove(skill);
            skillComponent.learnedSkills.put(skill, level);
            if (skill.equals(skillComponent.currentSkillInTraining)) {
                setSkillInTraining(skillComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
            }
            saveEntityEventualSkillComponent(entityRef, skillComponent);
            scheduleEntityTrainingCompletion(skillComponent, entityRef);
            logger.info(entityRef.toString() + " given skill " + skill + " level " + level);
        }
    }

    public void startTraining(EntityRef entityRef, ResourceUrn skillUrn) {
        if (skillUrn != null && entityRef != null) {
            EntityEventualSkillsComponent skillComponent = getEntityEventualSkillComponent(entityRef);
            if (eventualSkillsManager.getPrerequisiteSkillsNeeded(skillComponent, skillUrn).size() == 0) {
                savePartialTraining(skillComponent);
                setSkillInTraining(skillComponent, skillUrn);
                saveEntityEventualSkillComponent(entityRef, skillComponent);
                scheduleEntityTrainingCompletion(skillComponent, entityRef);
                logger.info(entityRef.toString() + " started training skill " + skillUrn.toString());
            } else {
                logger.info(entityRef.toString() + " does not have the prerequisites to train the skill " + skillUrn.toString());
            }
        }
    }

    public void stopTraining(EntityRef entityRef) {
        if (entityRef != null) {
            EntityEventualSkillsComponent skillComponent = getEntityEventualSkillComponent(entityRef);
            savePartialTraining(skillComponent);
            setSkillInTraining(skillComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
            saveEntityEventualSkillComponent(entityRef, skillComponent);
            scheduleEntityTrainingCompletion(skillComponent, entityRef);
            logger.info(entityRef.toString() + " stopped training skill");
        }
    }

    public void completeTraining(EntityRef entityRef) {
        if (entityRef != null) {
            EntityEventualSkillsComponent skillComponent = getEntityEventualSkillComponent(entityRef);
            String completedSkill = skillComponent.currentSkillInTraining;
            if (completedSkill != null) {
                calculatePartialTraining(skillComponent);
                if (skillComponent.currentSkillInTraining != null) {
                    skillComponent.learnedSkills.put(skillComponent.currentSkillInTraining, skillComponent.currentSkillLevelInTraining);
                    skillComponent.partiallyLearnedSkills.remove(skillComponent.currentSkillInTraining);

                    setSkillInTraining(skillComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
                    logger.info(entityRef.toString() + " completed training skill " + completedSkill);
                } else {
                    logger.info(entityRef.toString() + " could not completed training skill, no skill currently being trained");
                }
                saveEntityEventualSkillComponent(entityRef, skillComponent);
                scheduleEntityTrainingCompletion(skillComponent, entityRef);
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
        }
        delayManager.addDelayedAction(entityRef, DELAY_MANAGER_ACTION, delayAmount);
    }

    private void savePartialTraining(EntityEventualSkillsComponent skillComponent) {
        if (skillComponent.currentSkillInTraining != null) {
            calculatePartialTraining(skillComponent);
            skillComponent.partiallyLearnedSkills.put(skillComponent.currentSkillInTraining, skillComponent.currentTrainingCurrentSkillPoints);
        }
        skillComponent.currentSkillInTraining = null;
        skillComponent.currentTrainingCurrentSkillPoints = 0;
        skillComponent.currentSkillLevelInTraining = 0;
        skillComponent.currentSkillRankInTraining = 0;
        skillComponent.trainingLastTimeComputedSkillPoints = Long.MAX_VALUE;
    }

    private void calculatePartialTraining(EntityEventualSkillsComponent skillComponent) {
        long currentTime = time.getGameTimeInMs();
        long lastComputedTime = skillComponent.trainingLastTimeComputedSkillPoints;
        int newSkillPoints = (int) ((double) (currentTime - lastComputedTime) * SKILL_POINTS_PER_MILLISECOND);
        skillComponent.currentTrainingCurrentSkillPoints += newSkillPoints;
        skillComponent.trainingLastTimeComputedSkillPoints = currentTime;
    }

    private void setSkillInTraining(EntityEventualSkillsComponent skillComponent, ResourceUrn skillUrn) {
        String skill = skillUrn.toString();
        Optional<Prefab> skillPrefab = Assets.getPrefab(skill);
        if (!skillPrefab.isPresent() || !skillPrefab.get().hasComponent(EventualSkillDescriptionComponent.class)) {
            logger.warn("Skill, " + skill + " is not valid");
            return;
        }
        EventualSkillDescriptionComponent skillDescription = skillPrefab.get().getComponent(EventualSkillDescriptionComponent.class);

        // validate that the skill to be trained has all the prerequisiteSkills met
        for (Map.Entry<String, Integer> skillReq : skillDescription.prerequisiteSkills.entrySet()) {
            if (!skillComponent.hasSkill(new ResourceUrn(skillReq.getKey()), skillReq.getValue())) {
                logger.warn("Cannot train skill, " + skill + " the prerequisite has not been trained, " + skillReq.getKey() + " level " + skillReq.getValue());
                if (skill.equals(EventualSkillsCommonSystem.IDLE_SKILL_URN.toString())) {
                    // avoid stack overflow
                    return;
                }
                setSkillInTraining(skillComponent, EventualSkillsCommonSystem.IDLE_SKILL_URN);
                return;
            }
        }

        // load any partial training
        if (skillComponent.partiallyLearnedSkills.containsKey(skill)) {
            skillComponent.currentTrainingCurrentSkillPoints = skillComponent.partiallyLearnedSkills.get(skill);
        } else {
            skillComponent.currentTrainingCurrentSkillPoints = 0;
        }

        // get the next level to train
        skillComponent.currentSkillLevelInTraining = 1;
        if (skillComponent.learnedSkills.containsKey(skill)) {
            skillComponent.currentSkillLevelInTraining = skillComponent.learnedSkills.get(skill) + 1;
        }

        skillComponent.currentSkillRankInTraining = skillDescription.rank;
        skillComponent.currentTrainingTargetSkillPoints = eventualSkillsManager.skillPointsNeeded(skillComponent.currentSkillRankInTraining, skillComponent.currentSkillLevelInTraining);
        skillComponent.currentSkillInTraining = skill;
        skillComponent.trainingLastTimeComputedSkillPoints = time.getGameTimeInMs();
    }


    private EntityEventualSkillsComponent getEntityEventualSkillComponent(EntityRef entityRef) {
        EntityEventualSkillsComponent entityEventualSkillsComponent = entityRef.getComponent(EntityEventualSkillsComponent.class);
        if (entityEventualSkillsComponent == null) {
            entityEventualSkillsComponent = new EntityEventualSkillsComponent();
        }
        return entityEventualSkillsComponent;
    }

    private void saveEntityEventualSkillComponent(EntityRef entityRef, EntityEventualSkillsComponent entityEventualSkillsComponent) {
        if (entityRef.hasComponent(EntityEventualSkillsComponent.class)) {
            entityRef.saveComponent(entityEventualSkillsComponent);
        } else {
            entityRef.addComponent(entityEventualSkillsComponent);
        }
    }

    @ReceiveEvent
    public void onTrainingDelayCompletion(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(DELAY_MANAGER_ACTION)) {
            completeTraining(entityRef);
        }
    }

    @ReceiveEvent
    public void onRequestStartTraining(RequestStartTraining event, EntityRef entity) {
        startTraining(entity, new ResourceUrn(event.skill));
    }

    @ReceiveEvent
    public void onRequestStopTraining(RequestStopTraining event, EntityRef entity) {
        stopTraining(entity);
    }

    @Command(shortDescription = "Gives the skill to you", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String giveEventualSkill(
            @Sender EntityRef client,
            @CommandParam("Skill Resource Urn") String skillPrefabName,
            @CommandParam(value = "Level", required = false) Integer levelInput) {
        int level = 1;
        if (levelInput != null) {
            level = levelInput;
        }

        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        ResourceUrn skillUrn = new ResourceUrn(skillPrefabName);
        giveSkill(playerEntity, skillUrn, level);
        EntityEventualSkillsComponent entityEventualSkillsComponent = playerEntity.getComponent(EntityEventualSkillsComponent.class);
        if (entityEventualSkillsComponent.hasSkill(skillUrn, level)) {
            return "Skill given";
        } else {
            return "Error: skill not given";
        }
    }
}
