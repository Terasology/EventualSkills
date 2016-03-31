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

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.events.GiveSkillEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;

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
        EntityEventualSkillsComponent entityEventualSkillsComponent = playerEntity.getComponent(EntityEventualSkillsComponent.class);
        if (entityEventualSkillsComponent.getSkillLevel(skillUrn) == level) {
            return "Skill given";
        } else {
            return "Error: skill not given";
        }
    }
}
