// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.ui;

import com.google.common.base.Strings;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;

public class NoSkillsNagWidget extends CoreHudWidget {
    UILabel message;

    @Override
    public void initialise() {
        message = find("message", UILabel.class);
        if (message != null) {
            message.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    EntityRef targetEntity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                    EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                    if (skillsComponent == null || skillsComponent.learnedSkills.size() == 0) {
                        return "You have no skills, press 'L' to start training";
                    }
                    return null;
                }

            });
        }
        bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                if (message != null) {
                    return !Strings.isNullOrEmpty(message.getText());
                } else {
                    return false;
                }
            }
        });
    }
}
