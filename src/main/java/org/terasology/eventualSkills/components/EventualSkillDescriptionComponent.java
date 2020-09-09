// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.components;

import org.terasology.engine.entitySystem.Component;

import java.util.Map;
import java.util.TreeMap;

public class EventualSkillDescriptionComponent implements Component {
    public String name;
    public String description;
    public String shortName;
    public int rank = 1;
    // a map of skill urns and their minimum levels
    public Map<String, Integer> prerequisiteSkills = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);


}
