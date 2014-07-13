/**
 * Copyright (C) 2014 Sönke Sothmann
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

package de.richsource.gradle.plugins.resourcecaching;

import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class GenerateHostPage extends SourceTask {

  @InputFile File template
  @OutputFile File dest

  @TaskAction
  def generateHostPage() {
    String scriptTags = source.collect { f ->
      return "<script src=\"js/${f.name}\"></script>"
	}.join("\n")
	dest.text = template.text.replaceAll('@generatedScriptTags@', scriptTags)
  }
}