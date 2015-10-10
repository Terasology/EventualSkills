/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.eventualSkills.ui;

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.eventualSkills.components.EntityEventualSkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.events.RequestStartTraining;
import org.terasology.eventualSkills.events.RequestStopTraining;
import org.terasology.eventualSkills.systems.EventualSkillsManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventualSkillsTraining extends BaseInteractionScreen {
    UIList<ResourceUrn> skillList;
    UILabel selectedSkillName;
    UILabel selectedSkillProgress;
    UILabel selectedSkillPrerequisiteSkills;
    UILabel selectedSkillDescription;
    UIButton selectedSkillAction;

    List<ResourceUrn> allSkills;
    EntityRef targetEntity;
    ResourceUrn selectedSkillUrn;
    EventualSkillDescriptionComponent selectedSkill;


    @Override
    protected void initialise() {
        allSkills = new ArrayList<>();
        for (ResourceUrn skill : CoreRegistry.get(EventualSkillsManager.class).listSkills()) {
            allSkills.add(skill);
        }

        skillList = find("skillList", UIList.class);
        if (skillList != null) {
            skillList.bindList(new ReadOnlyBinding<List<ResourceUrn>>() {
                @Override
                public List<ResourceUrn> get() {
                    return allSkills;
                }
            });
            skillList.bindSelection(new Binding<ResourceUrn>() {
                @Override
                public ResourceUrn get() {
                    return selectedSkillUrn;
                }

                @Override
                public void set(ResourceUrn value) {
                    selectedSkillUrn = value;
                    selectedSkill = CoreRegistry.get(EventualSkillsManager.class).getSkill(value);
                }
            });
            skillList.setItemRenderer(new StringTextRenderer<ResourceUrn>() {
                @Override
                public String getString(ResourceUrn value) {
                    return CoreRegistry.get(EventualSkillsManager.class).getSkill(value).name;
                }

                @Override
                public void draw(ResourceUrn value, Canvas canvas) {
                    EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    if (targetSkills != null) {
                        if (value.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
                            canvas.setMode("training");
                        }
                    }
                    super.draw(value, canvas);
                }
            });
        }

        selectedSkillName = find("selectedSkillName", UILabel.class);
        if (selectedSkillName != null) {
            selectedSkillName.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (selectedSkill != null) {
                        int currentLevel = 0;
                        EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                        if (targetSkills != null) {
                            currentLevel = targetSkills.getSkillLevel(selectedSkillUrn);
                        }
                        return selectedSkill.name + " (" + selectedSkill.shortName + ") level " + currentLevel;
                    } else {
                        return null;
                    }
                }
            });
        }
        selectedSkillDescription = find("selectedSkillDescription", UILabel.class);
        if (selectedSkillDescription != null) {
            selectedSkillDescription.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (selectedSkill != null) {
                        return selectedSkill.description;
                    } else {
                        return null;
                    }
                }
            });
        }
        selectedSkillProgress = find("selectedSkillProgress", UILabel.class);
        if (selectedSkillProgress != null) {
            selectedSkillProgress.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    if (targetSkills != null && selectedSkillUrn != null) {
                        if (selectedSkillUrn.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
                            EventualSkillsManager skillsManager = CoreRegistry.get(EventualSkillsManager.class);
                            int totalSkillPoints = skillsManager.skillPointsNeeded(
                                    targetSkills.currentSkillRankInTraining,
                                    targetSkills.getSkillLevel(new ResourceUrn(targetSkills.currentSkillInTraining)) + 1);
                            int currentSkillPoints = skillsManager.calculateCurrentTrainingSkillPoints(targetSkills);
                            return "Level " + targetSkills.currentSkillLevelInTraining + " " + currentSkillPoints + "/" + totalSkillPoints;
                        } else {
                            int nextSkillLevel = targetSkills.getSkillLevel(selectedSkillUrn) + 1;
                            EventualSkillsManager skillsManager = CoreRegistry.get(EventualSkillsManager.class);
                            int totalSkillPoints = skillsManager.skillPointsNeeded(
                                    skillsManager.getSkill(selectedSkillUrn).rank,
                                    nextSkillLevel);
                            int currentSkillPoints = 0;
                            if (targetSkills.partiallyLearnedSkills.containsKey(selectedSkillUrn.toString())) {
                                currentSkillPoints = targetSkills.partiallyLearnedSkills.get(selectedSkillUrn.toString());
                            }
                            return "Level " + nextSkillLevel + " " + currentSkillPoints + "/" + totalSkillPoints;
                        }
                    } else {
                        return null;
                    }
                }
            });
        }

        selectedSkillPrerequisiteSkills = find("selectedSkillPrerequisiteSkills", UILabel.class);
        if (selectedSkillPrerequisiteSkills != null) {
            selectedSkillPrerequisiteSkills.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (selectedSkillUrn != null) {
                        EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                        EventualSkillsManager skillsManager = CoreRegistry.get(EventualSkillsManager.class);

                        String result = "";
                        Map<ResourceUrn, Integer> prerequisiteSkillsNeeded = skillsManager.getPrerequisiteSkillsNeeded(targetSkills, selectedSkillUrn);
                        for (Map.Entry<ResourceUrn, Integer> prereqSkill : prerequisiteSkillsNeeded.entrySet()) {
                            result += skillsManager.getSkill(prereqSkill.getKey()).name + " " + prereqSkill.getValue() + "\r\n";
                        }

                        return result;
                    } else {
                        return null;
                    }
                }
            });
        }

        selectedSkillAction = find("selectedSkillAction", UIButton.class);
        if (selectedSkillAction != null) {
            selectedSkillAction.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (selectedSkillUrn != null) {
                        EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                        if (targetSkills == null || !selectedSkillUrn.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
                            return "Start Training";
                        } else {
                            return "Stop Training";
                        }
                    }
                    return "";
                }
            });
            selectedSkillAction.bindVisible(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return selectedSkillUrn != null;
                }
            });
            selectedSkillAction.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    EventualSkillsManager skillsManager = CoreRegistry.get(EventualSkillsManager.class);

                    if (selectedSkill != null) {
                        if (targetSkills != null) {
                            Map<ResourceUrn, Integer> prerequisiteSkillsNeeded = skillsManager.getPrerequisiteSkillsNeeded(targetSkills, selectedSkillUrn);
                            return prerequisiteSkillsNeeded.size() == 0;
                        } else {
                            // allow starting training when a new player is made
                            return skillsManager.getPrerequisiteSkillsNeeded(new EntityEventualSkillsComponent(), selectedSkillUrn).size() == 0;
                        }
                    } else {
                        return false;
                    }
                }
            });
            selectedSkillAction.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (!selectedSkillAction.isEnabled()) {
                        return "Required skills not yet learned";
                    } else {
                        return null;
                    }
                }
            });
            selectedSkillAction.subscribe(new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget widget) {
                    if (selectedSkillUrn != null) {
                        EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                        if (targetSkills != null && selectedSkillUrn.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
                            targetEntity.send(new RequestStopTraining());
                        } else {
                            targetEntity.send(new RequestStartTraining(selectedSkillUrn));
                        }
                    }
                }
            });
        }
    }

    public void initializeWithTarget(EntityRef target) {
        targetEntity = target;
    }

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        initializeWithTarget(interactionTarget);
    }

}
