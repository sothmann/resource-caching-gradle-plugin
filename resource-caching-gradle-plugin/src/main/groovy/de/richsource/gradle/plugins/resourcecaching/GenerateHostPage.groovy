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
import org.gradle.api.tasks.Input

class GenerateHostPage extends SourceTask {

  @InputFile File template
  @Input String jsPath = 'js'
  @Input String cssPath = 'css'
  @OutputFile File dest

  @TaskAction
  def generateHostPage() {
	def jsFiles = source.findAll {it.name.endsWith(".js")}
	def cssFiles = source.findAll {it.name.endsWith(".css")}
    String scriptTags = generateScriptTags(jsFiles)
    String cssTags = generateCssLinkTags(cssFiles)
    dest.text = template.text.replaceAll('@generatedScriptTags@', scriptTags).replaceAll('@generatedCssTags@', cssTags)
  }
  
  private String generateScriptTags(jsFiles) {
    return jsFiles.collect { f ->
      return "<script src=\"${jsPath}/${f.name}\"></script>"
    }.join("\n")
  }
  
  private String generateCssLinkTags(cssFiles) {
    return cssFiles.collect { f ->
      return "<link rel=\"stylesheet\" type=\"text/css\" href=\"${cssPath}/${f.name}\">"
    }.join("\n")
  }
}