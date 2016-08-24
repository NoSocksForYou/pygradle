/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkedin.gradle.python.tasks;

import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.*;
import org.gradle.process.internal.ExecAction;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


@ParallelizableTask
public class PythonTestTask extends BasePythonTask {

    private static final Logger logger = Logging.getLogger(PythonTestTask.class);

    @OutputFile
    File outputFile;

    @SkipWhenEmpty
    @InputDirectory
    Set<File> testSources = new HashSet<File>();

    @TaskAction
    public void runTests() {
        final File pyTest = new File(getVenvDir(), "bin/py.test");
        if (!pyTest.exists()) {
            throw new PyTestNotFoundException();
        }
        execute(new Action<ExecAction>() {
            @Override
            public void execute(ExecAction execAction) {
                execAction.args(pyTest.getAbsoluteFile());
                execAction.args(String.format("--junit-xml=%s", outputFile.getAbsolutePath()));
                execAction.args(String.format("--ignore=%s", getPythonEnvironment().getBuildDir().getAbsolutePath()));
                for (File testSource : testSources) {
                    logger.debug("Checking source dir {}, exists {}", testSource, testSource.exists());
                    if (testSource.exists()) {
                        execAction.args(testSource);
                    }
                }
            }
        });
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void registerTestSources(Set<File> sources) {
        testSources.addAll(sources);
    }

    public static class PyTestNotFoundException extends RuntimeException {
    }
}