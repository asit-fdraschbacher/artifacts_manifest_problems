package com.a2p2.stage.injectstaticlibrary;

import com.a2p2.deployment.core.apk.AndroidPackage;
import com.a2p2.deployment.core.axml.*;
import com.a2p2.deployment.core.exceptions.*;
import com.a2p2.deployment.core.manifest.ManifestPatcher;
import com.a2p2.deployment.core.manifest.PackageInfo;
import com.a2p2.deployment.core.manifest.PackageInfoExtractor;
import com.a2p2.deployment.core.pipeline.*;
import com.a2p2.deployment.core.utils.IOUtils;

import java.io.*;
import java.util.List;

/**
 * Applies a patch package to an Android application package.
 */
public class InjectStaticLibraryStage extends Stage {
    PipelineContext context = null;

    private String libraryName = null;
    private String libraryVersion = null;
    private String libraryCertDigest = null;

    public InjectStaticLibraryStage(List<String> arguments) throws StageInitException {
        super(arguments);

        // Null arguments are passed when registering stage
        if (arguments == null) return;

        if (arguments.size() == 3) {
            libraryName = arguments.get(0);
            libraryVersion = arguments.get(1);
            libraryCertDigest = arguments.get(2);
        } else {
            throw new StageInitException("No valid arguments provided!");
        }
    }

    public String getName() {
        return "injectstaticlibrary";
    }

    @Override
    public String getProgressDescription() {
        return "Injecting static library dependency...";
    }

    @Override
    public String getDescription() {
        return "Injects a static library dependency into an APK";
    }

    @Override
    public String getUsage() {
        return getName() + " <library_name> <library_version> <library_cert_digest>";
    }

    @Override
    public double getTotalProgressFactor() {
        return 0.1;
    }

    private PackageInfo extractPackageInfo(AndroidPackage apk) throws ExtractInfoException {
        PackageInfoExtractor extractor = new PackageInfoExtractor(apk);
        try {
            return extractor.extract();
        } catch (IOException e) {
            throw new ExtractInfoException("I/O error while extracting application manifest", e);
        } catch (InvalidManifestException e) {
            throw new ExtractInfoException("Could not extract invalid application manifest", e);
        }
    }

    @Override
    public void run(PipelineContext context, ProgressListener progressListener) throws StageRunException {
        if (context.getSourceApk() != context.getBaseApk()) return;

        try {
            AxmlTree.Node root = context.getSourceApk().getManifest();
            AxmlNamespace androidNamespace = root.getNamespace("http://schemas.android.com/apk/res/android");
            if (androidNamespace == null) {
                androidNamespace = new AxmlNamespace("android", "http://schemas.android.com/apk/res/android");
                root.addNamespace(androidNamespace);
            }

            List<AxmlTree.Node> manifestChildren = root.getChildrenWithName(new AxmlNamespacedName("manifest"));
            if (manifestChildren.size() != 1) {
                throw new InvalidManifestException("Android manifest lacks root manifest node!");
            }

            AxmlTree.Node manifest = (AxmlTree.Node) manifestChildren.get(0);
            List<AxmlTree.Node> applicationChildren = manifest.getChildrenWithName(new AxmlNamespacedName("application"));
            if (applicationChildren.size() != 1) {
                throw new InvalidManifestException("Android manifest lacks application node!");
            }

            AxmlTree.Node application = (AxmlTree.Node) applicationChildren.get(0);
            AxmlTree.Node usesStaticLibrary = new AxmlTree.Node(new AxmlNamespacedName("uses-static-library"));
            usesStaticLibrary.setAttribute(new AxmlNamespacedName(androidNamespace, "name"),
                    new AxmlAttribute(AxmlAttribute.Type.STRING, libraryName, Attrs.name));
            usesStaticLibrary.setAttribute(new AxmlNamespacedName(androidNamespace, "version"),
                    new AxmlAttribute(AxmlAttribute.Type.STRING, libraryVersion, Attrs.version));
            usesStaticLibrary.setAttribute(new AxmlNamespacedName(androidNamespace, "certDigest"),
                    new AxmlAttribute(AxmlAttribute.Type.STRING, libraryCertDigest,
                            Attrs.certDigest));
            application.addChild(usesStaticLibrary);

            byte[] mainManifestData = AxmlTree.toAxml(root);
            IOUtils.writeDataToFile(mainManifestData, new File(context.getWorkingFolder(), AndroidPackage.PATH_MANIFEST));
        } catch (Exception e) {
            throw new StageRunException(e);
        }
    }
}