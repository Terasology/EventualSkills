// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.events.GiveSkillEvent;
import org.terasology.gestalt.assets.ResourceUrn;

@RegisterSystem
public class SkillsCommonSystem extends BaseComponentSystem {
    @Command(shortDescription = "Gives the skill to you", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String giveSkill(
            @Sender EntityRef client,
            @CommandParam("Skill Resource Urn") String skillPrefabName,
            @CommandParam(value = "Level", required = false) Integer levelInput) {
        int level = 1;
        if (levelInput != null) {
            level = levelInput;
        }

        EntityRef playerEntity = client.getComponent(ClientComponent.class).character;
        ResourceUrn skillUrn = new ResourceUrn(skillPrefabName);
        playerEntity.send(new GiveSkillEvent(skillUrn.toString(), level));
        EntitySkillsComponent skillsComponent = playerEntity.getComponent(EntitySkillsComponent.class);
        if (skillsComponent != null && skillsComponent.getSkillLevel(skillUrn) == level) {
            return "Skill given";
        } else {
            return "Error: skill not given";
        }
    }
}
