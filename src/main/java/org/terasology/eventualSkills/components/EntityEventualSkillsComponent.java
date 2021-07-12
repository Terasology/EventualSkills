// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.components;

import com.google.common.collect.Maps;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class EntityEventualSkillsComponent implements Component<EntityEventualSkillsComponent> {
    @Replicate
    public String currentSkillInTraining;
    @Replicate
    public int currentSkillLevelInTraining;
    @Replicate
    public int currentSkillRankInTraining;
    @Replicate
    public int currentTrainingTargetSkillPoints;
    @Replicate
    public int currentTrainingCurrentSkillPoints;
    @Replicate
    public long trainingLastTimeComputedSkillPoints;

    // a map of the skill and how many skill points have already been acquired.  These must always be lowercased, otherwise chaos.
    @Replicate
    public Map<String, Integer> partiallyLearnedSkills = Maps.newHashMap();

    @Override
    public void copy(EntityEventualSkillsComponent other) {
        this.currentSkillInTraining = other.currentSkillInTraining;
        this.currentSkillLevelInTraining = other.currentSkillLevelInTraining;
        this.currentSkillRankInTraining = other.currentSkillRankInTraining;
        this.currentTrainingTargetSkillPoints = other.currentTrainingTargetSkillPoints;
        this.currentTrainingCurrentSkillPoints = other.currentTrainingCurrentSkillPoints;
        this.trainingLastTimeComputedSkillPoints = other.trainingLastTimeComputedSkillPoints;
        this.partiallyLearnedSkills.clear();
        this.partiallyLearnedSkills.putAll(other.partiallyLearnedSkills);
    }
}
