// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.components;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.Map;

public class EntityEventualSkillsComponent implements Component {
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

    // a map of the skill and how many skill points have already been acquired.  These must always be lowercased, 
    // otherwise chaos.
    @Replicate
    public Map<String, Integer> partiallyLearnedSkills = Maps.newHashMap();
}
