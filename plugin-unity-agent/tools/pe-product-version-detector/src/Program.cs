

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