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

import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest;

class HashFiles extends SourceTask {

	@Input String hashAlgorithm = "SHA-1"
	@OutputDirectory File dest

	@TaskAction
	def hashFiles() {
		dest.eachFileRecurse { File f ->
			f.delete()
		}
		
		source.each { File f ->
			MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
			Formatter hexHash = new Formatter()
			digest.digest(f.bytes).each { b -> hexHash.format('%02x', b) }
			String extension = f.name.substring(f.name.lastIndexOf('.')+1)
			File out = new File(dest, "${hexHash.toString()}.cache.${extension}")
			ant.copy(file: "$f.canonicalPath", tofile: "$out.canonicalPath")
		}
	}
}