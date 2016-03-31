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
import org.terasology.eventualSkills.components.EntitySkillsComponent;
import org.terasology.eventualSkills.components.EventualSkillDescriptionComponent;
import org.terasology.eventualSkills.events.StartTrainingSkillRequestEvent;
import org.terasology.eventualSkills.events.StopTrainingSkillRequestEvent;
import org.terasology.eventualSkills.systems.EventualSkillsManager;
import org.terasology.registry.In;
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
    @In
    EventualSkillsManager eventualSkillsManager;

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
        for (ResourceUrn skill : eventualSkillsManager.listSkills()) {
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
                    selectedSkill = eventualSkillsManager.getSkill(value);
                }
            });
            skillList.setItemRenderer(new StringTextRenderer<ResourceUrn>() {
                @Override
                public String getString(ResourceUrn value) {
                    return eventualSkillsManager.getSkill(value).name;
                }

                @Override
                public void draw(ResourceUrn value, Canvas canvas) {
                    EntityEventualSkillsComponent targetSkills = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    if (targetSkills != null) {
                        if (targetSkills.currentSkillInTraining != null && value.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
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
                        EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                        if (skillsComponent != null) {
                            currentLevel = skillsComponent.getSkillLevel(selectedSkillUrn);
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
                    EntityEventualSkillsComponent eventualSkillsComponent = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                    if (skillsComponent != null && eventualSkillsComponent != null && selectedSkillUrn != null) {
                        if (eventualSkillsComponent.currentSkillInTraining != null && selectedSkillUrn.equals(new ResourceUrn(eventualSkillsComponent.currentSkillInTraining))) {
                            EventualSkillsManager skillsManager = eventualSkillsManager;
                            int totalSkillPoints = skillsManager.skillPointsNeeded(
                                    eventualSkillsComponent.currentSkillRankInTraining,
                                    skillsComponent.getSkillLevel(new ResourceUrn(eventualSkillsComponent.currentSkillInTraining)) + 1);
                            int currentSkillPoints = skillsManager.calculateCurrentTrainingSkillPoints(eventualSkillsComponent);
                            return "Level " + eventualSkillsComponent.currentSkillLevelInTraining + " " + currentSkillPoints + "/" + totalSkillPoints;
                        } else {
                            int nextSkillLevel = skillsComponent.getSkillLevel(selectedSkillUrn) + 1;
                            EventualSkillsManager skillsManager = eventualSkillsManager;
                            int totalSkillPoints = skillsManager.skillPointsNeeded(
                                    skillsManager.getSkill(selectedSkillUrn).rank,
                                    nextSkillLevel);
                            int currentSkillPoints = 0;
                            if (eventualSkillsComponent.partiallyLearnedSkills.containsKey(selectedSkillUrn.toString().toLowerCase())) {
                                currentSkillPoints = eventualSkillsComponent.partiallyLearnedSkills.get(selectedSkillUrn.toString().toLowerCase());
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
                        EntityEventualSkillsComponent eventualSkillsComponent = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                        EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                        EventualSkillsManager skillsManager = eventualSkillsManager;

                        String result = "";
                        Map<ResourceUrn, Integer> prerequisiteSkillsNeeded = skillsManager.getPrerequisiteSkillsNeeded(skillsComponent, eventualSkillsComponent, selectedSkillUrn);
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
                        if (targetSkills == null || targetSkills.currentSkillInTraining == null || !selectedSkillUrn.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
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
                    EntityEventualSkillsComponent eventualSkillsComponent = targetEntity.getComponent(EntityEventualSkillsComponent.class);
                    EntitySkillsComponent skillsComponent = targetEntity.getComponent(EntitySkillsComponent.class);
                    EventualSkillsManager skillsManager = eventualSkillsManager;

                    if (selectedSkill != null) {
                        if (eventualSkillsComponent != null) {
                            Map<ResourceUrn, Integer> prerequisiteSkillsNeeded = skillsManager.getPrerequisiteSkillsNeeded(skillsComponent, eventualSkillsComponent, selectedSkillUrn);
                            return prerequisiteSkillsNeeded.size() == 0;
                        } else {
                            // allow starting training when a new player is made
                            return skillsManager.getPrerequisiteSkillsNeeded(new EntitySkillsComponent(), new EntityEventualSkillsComponent(), selectedSkillUrn).size() == 0;
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
                        if (targetSkills != null && targetSkills.currentSkillInTraining != null && selectedSkillUrn.equals(new ResourceUrn(targetSkills.currentSkillInTraining))) {
                            targetEntity.send(new StopTrainingSkillRequestEvent());
                        } else {
                            targetEntity.send(new StartTrainingSkillRequestEvent(selectedSkillUrn));
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
