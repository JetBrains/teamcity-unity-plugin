/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// See https://aka.ms/new-console-template for more information

using System;
using System.Diagnostics;
using System.IO;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace PeProductVersionDetector;

class Program
{
    static int Main(string[] args)
    {
        if (args.Length < 1)
        {
            Console.Error.WriteLine("No file provided");
            return -1;
        }
        
        var executable = args[0];

        if (!File.Exists(executable))
        {
            Console.Error.WriteLine($"Specified file {executable} does not exist");
            return -1;
        }
        
        try
        {
            var versionInfo = FileVersionInfo.GetVersionInfo(executable);
            var result = JsonSerializer.Serialize(new ProductVersion(
                versionInfo.ProductMajorPart,
                versionInfo.ProductMinorPart,
                versionInfo.ProductBuildPart,
                versionInfo.ProductPrivatePart),
                SourceGenerationContext.Default.ProductVersion
            );
            Console.Out.WriteLine(result);
            return 0;
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e.ToString());
            return -1;
        }
    }
}

internal record ProductVersion(
    int? MajorPart,
    int? MinorPart,
    int? BuildPart,
    int? PrivatePart
);

[JsonSerializable(typeof(ProductVersion))]
internal partial class SourceGenerationContext : JsonSerializerContext
{
}