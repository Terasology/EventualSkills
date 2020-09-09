// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.systems;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.config.ModuleConfigManager;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.Assets;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.components.SkillGivingItemComponent;
import org.terasology.eventualSkills.events.StartTrainingSkillRequestEvent;
import org.terasology.eventualSkills.events.StopTrainingSkillRequestEvent;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RegisterSystem
@Share(EventualSkillsManager.class)
public class EventualSkillsCommonSystem extends BaseComponentSystem implements EventualSkillsManager {
    public static final ResourceUrn IDLE_SKILL_URN = new ResourceUrn("EventualSkills:IdleSkill");
    private static final Logger logger = LoggerFactory.getLogger(EventualSkillsCommonSystem.class);
    @In
    PrefabManager prefabManager;
    @In
    Time time;
    @In
    ModuleConfigManager moduleConfigManager;
    @In
    EntityManager entityManager;

    @Override
    public Iterable<ResourceUrn> listSkills() {
        List<ResourceUrn> items = new ArrayList<>();
        for (Prefab skillPrefab : prefabManager.listPrefabs(EventualSkillDescriptionComponent.class)) {
            items.add(skillPrefab.getUrn());
        }
        return items;
    }

    @Override
    public EventualSkillDescriptionComponent getSkill(ResourceUrn skillUrn) {
        Optional<Prefab> skillPrefab = Assets.getPrefab(skillUrn.toString());
        if (skillPrefab.isPresent()) {
            return skillPrefab.get().getComponent(EventualSkillDescriptionComponent.class);
        } else {
            logger.warn("Skill does not exist: " + skillUrn.toString());
            return null;
        }
    }

    @Override
    public Map<EventualSkillDescriptionComponent, Integer> getPrerequisiteSkills(ResourceUrn skillUrn) {
        Map<EventualSkillDescriptionComponent, Integer> items = Maps.newHashMap();
        for (Map.Entry<String, Integer> prerequisiteSkill : getSkill(skillUrn).prerequisiteSkills.entrySet()) {
            items.put(getSkill(new ResourceUrn(prerequisiteSkill.getKey())), prerequisiteSkill.getValue());
        }
        return items;
    }

    @Override
    public Map<ResourceUrn, Integer> getPrerequisiteSkillsNeeded(EntitySkillsComponent skillsComponent,
                                                                 EntityEventualSkillsComponent eventualSkillsComponent, ResourceUrn skillUrn) {
        EventualSkillDescriptionComponent skillDescription = getSkill(skillUrn);
        Map<ResourceUrn, Integer> prerequisiteSkillsNeeded = new HashMap<>();
        for (Map.Entry<String, Integer> prereqSkill : skillDescription.prerequisiteSkills.entrySet()) {
            ResourceUrn prereqSkillUrn = new ResourceUrn(prereqSkill.getKey());
            if (eventualSkillsComponent == null || !skillsComponent.hasSkill(prereqSkillUrn, prereqSkill.getValue())) {
                prerequisiteSkillsNeeded.put(prereqSkillUrn, prereqSkill.getValue());
            }
        }
        return prerequisiteSkillsNeeded;
    }

    @Override
    public int calculateCurrentTrainingSkillPoints(EntityEventualSkillsComponent eventualSkillsComponent) {
        long currentTime = time.getGameTimeInMs();
        long lastComputedTime = eventualSkillsComponent.trainingLastTimeComputedSkillPoints;
        int newSkillPoints =
                (int) ((double) (currentTime - lastComputedTime) * EventualSkillAuthoritySystem.SKILL_POINTS_PER_MILLISECOND);
        return newSkillPoints + eventualSkillsComponent.currentTrainingCurrentSkillPoints;
    }

    @Override
    public int skillPointsNeeded(int rank, int level) {
        if (rank <= 0 || level <= 0) {
            return 0;
        }

        float skillpointMultiplier = moduleConfigManager.getFloatVariable("EventualSkills", "skillpointMultiplier",
                1.0f);
        float skillpointScaling = moduleConfigManager.getFloatVariable("EventualSkills", "skillpointScaling", 1.0f);

        return (int) (skillpointMultiplier * rank * 250.0 * Math.pow(5.66, (level - 1) * skillpointScaling));
    }

    @Command(shortDescription = "Starts training a skill for you", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String startTrainingEventualSkill(
            @Sender EntityRef client,
            @CommandParam("Skill prefab name") String skillPrefabName) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        playerEntity.send(new StartTrainingSkillRequestEvent(new ResourceUrn(skillPrefabName)));
        return "Training started";
    }

    @Command(shortDescription = "Stops training current skill", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String stopTrainingEventualSkill(
            @Sender EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        playerEntity.send(new StopTrainingSkillRequestEvent());
        return "Training stopped";
    }

    @Command(shortDescription = "Details of the current skill being trained", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String currentTrainingEventualSkill(
            @Sender EntityRef client) {
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        EntityEventualSkillsComponent skillsComponent = playerEntity.getComponent(EntityEventualSkillsComponent.class);
        if (skillsComponent != null && skillsComponent.currentSkillInTraining != null) {
            EventualSkillDescriptionComponent skillDescription =
                    getSkill(new ResourceUrn(skillsComponent.currentSkillInTraining));
            return "Training: " + skillDescription.name + " level " + skillsComponent.currentSkillLevelInTraining + " "
                    + calculateCurrentTrainingSkillPoints(skillsComponent) + "/" + skillsComponent.currentTrainingTargetSkillPoints;
        }

        return "No training in progress";
    }

    @Command(shortDescription = "Gives you a skill book of the specified skill", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String giveSkillBook(
            @Sender EntityRef client,
            @CommandParam("skill") String skill,
            @CommandParam(value = "level", required = false) Integer level) {
        ResourceUrn skillUrn = new ResourceUrn(skill);
        EntityBuilder itemBuilder = entityManager.newBuilder("EventualSkills:SkillBook");
        SkillGivingItemComponent skillGivingItemComponent = new SkillGivingItemComponent();
        skillGivingItemComponent.skill = skillUrn.toString();
        skillGivingItemComponent.level = level;
        itemBuilder.addComponent(skillGivingItemComponent);

        EventualSkillDescriptionComponent eventualSkillDescriptionComponent = getSkill(skillUrn);
        if (eventualSkillDescriptionComponent == null) {
            return "Skill not found";
        }
        DisplayNameComponent displayNameComponent = new DisplayNameComponent();
        displayNameComponent.name = eventualSkillDescriptionComponent.name + " " + (level == null ? "+1" :
                level.toString()) + " skill book";
        itemBuilder.addComponent(displayNameComponent);

        EntityRef item = itemBuilder.build();
        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        GiveItemEvent giveItemEvent = new GiveItemEvent(playerEntity);
        item.send(giveItemEvent);
        if (!giveItemEvent.isHandled()) {
            item.destroy();
        }
        return "You received a " + skill + " skill book";
    }
}
