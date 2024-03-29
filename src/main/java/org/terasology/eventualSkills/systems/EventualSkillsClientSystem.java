// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.systems;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.unicode.EnclosedAlphanumerics;
import org.terasology.eventualSkills.EventualSkillsButton;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.ui.EventualSkillsTraining;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;

@RegisterSystem(RegisterMode.CLIENT)
public class EventualSkillsClientSystem extends BaseComponentSystem {

    private static final String NOTIFICATION_ID = "EventualSkills:noSkills";

    @In
    LocalPlayer localPlayer;
    @In
    NUIManager nuiManager;
    @In
    InputSystem inputSystem;

    @Override
    public void initialise() {
        super.initialise();
        nuiManager.getHUD().addHUDElement("EventualSkills:NoSkillsNagWidget");
    }

    /**
     * Get a formatted representation of the primary {@link Input} associated with the given button binding.
     * <p>
     * If the display name of the primary bound key is a single character this representation will be the encircled
     * character. Otherwise the full display name is used. The bound key will be printed in yellow.
     * <p>
     * If no key binding was found the text "n/a" in red color is returned.
     *
     * @param button the URI of a bindable button
     * @return a formatted text to be used as representation for the player
     */
    //TODO: put this in a common place? Duplicated in Dialogs and InGameHelp
    private String getActivationKey(SimpleUri button) {
        return inputSystem.getInputsForBindButton(button).stream()
                .findFirst()
                .map(Input::getDisplayName)
                .map(key -> {
                    if (key.length() == 1) {
                        // print the key in yellow within a circle
                        int off = key.charAt(0) - 'A';
                        char code = (char) (EnclosedAlphanumerics.CIRCLED_LATIN_CAPITAL_LETTER_A + off);
                        return String.valueOf(code);
                    } else {
                        return key;
                    }
                })
                .map(key -> FontColor.getColored(key, Color.yellow))
                .orElse(FontColor.getColored("n/a", Color.red));
    }

    private void showNoSkillsNotification() {
        Notification notification =
                new Notification(NOTIFICATION_ID,
                        "The Skill-less",
                        "Press " + getActivationKey(new SimpleUri("EventualSkills:eventualSkills")) + " to start " +
                                "training",
                        "CoreAssets:items#GooeysFist");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }

    @ReceiveEvent
    public void onLocalPlayerInitialized(LocalPlayerInitializedEvent event, EntityRef entity) {
        EntityRef targetEntity = localPlayer.getCharacterEntity();
        EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
        if (skillsComponent == null || skillsComponent.learnedSkills.size() == 0) {
            showNoSkillsNotification();
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onEventualSkillsButton(EventualSkillsButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            localPlayer.getClientEntity().send(new ExpireNotificationEvent(NOTIFICATION_ID));
            nuiManager.toggleScreen("EventualSkills:EventualSkillsTraining");
            EventualSkillsTraining screen =
                    (EventualSkillsTraining) nuiManager.getScreen("EventualSkills:EventualSkillsTraining");
            screen.initializeWithTarget(localPlayer.getCharacterEntity());

            event.consume();
        }
    }
}
