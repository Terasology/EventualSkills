// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.systems;

import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Map;

public interface EventualSkillsManager {
    Iterable<ResourceUrn> listSkills();

    EventualSkillDescriptionComponent getSkill(ResourceUrn skillUrn);

    Map<EventualSkillDescriptionComponent, Integer> getPrerequisiteSkills(ResourceUrn skillUrn);

    Map<ResourceUrn, Integer> getPrerequisiteSkillsNeeded(EntitySkillsComponent skillsComponent,
                                                          EntityEventualSkillsComponent eventualSkillsComponent,
                                                          ResourceUrn skillUrn);

    int skillPointsNeeded(int rank, int level);

    int calculateCurrentTrainingSkillPoints(EntityEventualSkillsComponent eventualSkillsComponent);
}
