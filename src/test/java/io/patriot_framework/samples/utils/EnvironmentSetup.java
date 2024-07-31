/*
 * Copyright 2019 Patriot project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.patriot_framework.samples.utils;

import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.junit.extensions.PatriotSetupExtension;

import java.util.UUID;

public class EnvironmentSetup extends PatriotSetupExtension {
    private static boolean setUp = false;
    private static UUID extensionsUUID = UUID.randomUUID();
    private DockerDeployment d = new DockerDeployment();

    public EnvironmentSetup() throws PropertiesNotLoadedException {
    }

    public void setUp() {
//        d.createTopology();
//        d.deployApplications();
        setUp = true;
    }

    public void tearDown() {
//        d.stopDocker();
    }

    protected UUID getUUID() {
        return extensionsUUID;
    }

    protected boolean isSetUp() {
        return setUp;
    }
}
