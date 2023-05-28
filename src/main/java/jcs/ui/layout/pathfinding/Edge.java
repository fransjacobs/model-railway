/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.ui.layout.pathfinding;

/**
 *
 * @author fransjacobs
 */
public class Edge {

    private final String sourceId;
    private final String targetId;
    private final double cost;

    public Edge(String sourceId, String targetId, double cost) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.cost = cost;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "Edge (" + sourceId + " -> " + targetId + ") distance: " + cost;
    }

}
