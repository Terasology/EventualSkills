// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.eventualSkills.EventualSkillsButton;
import org.terasology.eventualSkills.ui.EventualSkillsTraining;
import org.terasology.nui.input.ButtonState;

@RegisterSystem(RegisterMode.CLIENT)
public class EventualSkillsClientSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    NUIManager nuiManager;

    @Override
    public void initialise() {
        super.initialise();
        nuiManager.getHUD().addHUDElement("EventualSkills:NoSkillsNagWidget");
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onEventualSkillsButton(EventualSkillsButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            nuiManager.toggleScreen("EventualSkills:EventualSkillsTraining");
            EventualSkillsTraining screen = (EventualSkillsTraining) nuiManager.getScreen("EventualSkills" +
                    ":EventualSkillsTraining");
            screen.initializeWithTarget(localPlayer.getCharacterEntity());

            event.consume();
        }
    }
}
