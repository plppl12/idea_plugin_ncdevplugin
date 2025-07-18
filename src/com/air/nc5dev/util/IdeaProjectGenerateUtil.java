package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.exportpatcher.searchs.AbstractContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaClientFileContentSearchImpl;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.util.exportpatcher.output.SimpleCopyOutPutFileImpl;
import com.air.nc5dev.util.exportpatcher.beforafter.AbstarctBeforRule;
import com.air.nc5dev.util.exportpatcher.beforafter.EmptyBeforRule;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaFileContentSearchImpl;
import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.idea.RunConfigurationUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.ShortenCommandLine;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/***
 *    生成新项目里 NC必须的东西  等    <br>
 *     1.NC库依赖      <br>
 *     2.NC 3个源文件夹 和 1个xml     <br>
 *     3.NC 2个运行选项 服务端+客户端
 *     4. 复制项目 ump文件到 NC HOME的工具方法 <br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 9:02
 */
public class IdeaProjectGenerateUtil {
    /**
     * 生成NC默认 几个个文件夹 src和META-INF 到项目
     *
     * @param project
     */
    public static void generateSrcDir(@Nullable Project project) {
        Project project1 = project == null ? ProjectUtil.getDefaultProject() : project;

        if (!ProjectNCConfigUtil.hasSetNCHOME()) {
            return;
        }

        Module[] modules = IdeaProjectGenerateUtil.getProjectModules(project1);
        for (Module module : modules) {
            generateSrcDir(module);
            generatePatherConfigFile(module);
        }
    }

    /**
     * 生成NC默认 几个个文件夹 src和META-INF 等文件夹 到项目
     *
     * @param module
     */
    public static void generateSrcDir4Modules(Module module) {
        ProjectUtil.setProject(module.getProject());
        if (ProjectNCConfigUtil.getNCHome() == null) {
            return;
        }

        generateSrcDir(module);
        generatePatherConfigFile(module);
    }

    private static File createDirIfNotExists(File root, String... names) {
        String p = "";
        for (String name : names) {
            p += File.separatorChar + name;
        }
        File f = new File(root, p);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    /**
     * 生成NC默认 几个个文件夹 src和META-INF 到指定模块       <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/18 0018 12:25
     * @Param [module]
     */
    public static void generateSrcDir(@NotNull Module module) {
        if (!ProjectNCConfigUtil.hasSetNCHOME(module.getProject())) {
            return;
        }

        File homeDir = new File(module.getModuleFilePath()).getParentFile();
        File src = new File(homeDir, "src");
        createDirIfNotExists(src, "public");
        createDirIfNotExists(src, "private");
        createDirIfNotExists(src, "client");
        createDirIfNotExists(src, "test");
        createDirIfNotExists(homeDir, "META-INF");
        createDirIfNotExists(homeDir, "METADATA");
        createDirIfNotExists(homeDir, "resources");
        createDirIfNotExists(homeDir, "designmodel", "ace");
        createDirIfNotExists(homeDir, "designmodel", "coderule");
        createDirIfNotExists(homeDir, "designmodel", "funcmodel");
        createDirIfNotExists(homeDir, "designmodel", "systemplatebase");
        createDirIfNotExists(homeDir, "designmodel", "templet");
        createDirIfNotExists(homeDir, "config", "billcodepredata");
        createDirIfNotExists(homeDir, "config", "doc-lucene-config");
        createDirIfNotExists(homeDir, "config", "pfxx");
        createDirIfNotExists(homeDir, "config", "tabconfig");
        createDirIfNotExists(homeDir, "script", "dbcreate");
        createDirIfNotExists(homeDir, "script", "dbml", "simpchn");
        createDirIfNotExists(homeDir, "script", "conf", "pdm");
        File initdata = createDirIfNotExists(homeDir, "script", "conf", "initdata");

        File umpFile = new File(new File(homeDir, "META-INF"), "module.xml");
        if (!umpFile.exists()) {
            try {
                PrintWriter out = new PrintWriter(new FileOutputStream(umpFile));
                out.print(
                        StrUtil.replace(ProjectUtil.getResourceTemplatesUtf8Txt("module.xml")
                                , "${moduleName}", module.getName())
                );
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        copyProjectMetaInfFiles2NCHomeModules();

    }

    /**
     * 生成 插件补丁 标准导出配置文件 到 到指定模块       <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/18 0018 12:25
     * @Param [module]
     */
    public static void generatePatherConfigFile(@NotNull Module module) {
        if (!ProjectNCConfigUtil.hasSetNCHOME(module.getProject())) {
            return;
        }

        File homeDir = new File(module.getModuleFilePath()).getParentFile();
        File outFile = new File(homeDir, "patcherconfig.properties");
        if (!outFile.exists()) {
            try {
                PrintWriter out = new PrintWriter(outFile, "UTF-8");
                String txt = ProjectUtil.getResourceTemplatesUtf8Txt("patcherconfig.properties");
                txt = StrUtil.replace(txt, "${moduleName}", module.getName());
                txt = StrUtil.replace(txt, "${modulePath}", homeDir.getPath());
                out.print(txt);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Module getRunMenuNCServiceModel(Project project) {
        if (!checkNCHomeSetPass()) {
            return null;
        }

        RunManagerImpl runManager = RunConfigurationUtil.getRunManagerImpl(ProjectUtil.getDefaultProject());
        List<RunConfiguration> configurationsList = runManager.getConfigurationsList(ApplicationConfigurationType
                .getInstance());

        if (CollUtil.isEmpty(configurationsList)) {
            return null;
        }

        for (RunConfiguration cf : configurationsList) {
            ApplicationConfiguration c = (ApplicationConfiguration) cf;
            if ("ufmiddle.start.tomcat.StartDirectServer".equals(c.getMainClassName())) {
                return c.getConfigurationModule().getModule();
            }
        }

        return null;
    }

    /**
     * 生成 项目的 NC2个运行配置
     *
     * @param project
     */
    public static void generateRunMenu(@NotNull Project project) {
        if (!checkNCHomeSetPass()) {
            return;
        }

        File ncHome = ProjectNCConfigUtil.getNCHome();

        RunManagerImpl runManager = RunConfigurationUtil.getRunManagerImpl(ProjectUtil.getDefaultProject());
        List<RunConfiguration> configurationsList = runManager.getConfigurationsList(ApplicationConfigurationType
                .getInstance());

        final byte[] hasNc = new byte[2];
        final String serverClass = "ufmiddle.start.tomcat.StartDirectServer";
        final String clientClass = "nc.starter.test.JStarter";

        if (null != configurationsList && !configurationsList.isEmpty()) {
            //已经有配置了，检查是否有了NC的，有的要更新，没有的要新增
            configurationsList.stream().forEach(rc -> {
                ApplicationConfiguration conf = (ApplicationConfiguration) rc;
                if (serverClass.equals(((ApplicationConfiguration) rc).MAIN_CLASS_NAME)) {
                    hasNc[0] = 1;
                    //更新配置
                    Map<String, String> envs = conf.getEnvs();
                    //FIELD_NC_HOME
                    //FIELD_EX_MODULES
                    envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
                    envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
                    conf.setEnvs(envs);
                    conf.setWorkingDirectory(ncHome.getPath());

                    if (conf.getBeforeRunTasks() != null) {
                        conf.setBeforeRunTasks(new ArrayList<>(conf.getBeforeRunTasks()));
                    } else {
                        conf.setBeforeRunTasks(new ArrayList<>());
                    }
                    conf.getBeforeRunTasks().add(new CompileStepBeforeRun.MakeBeforeRunTask());
                } else if (clientClass.equals(((ApplicationConfiguration) rc).MAIN_CLASS_NAME)) {
                    hasNc[1] = 1;
                    //更新配置
                    Map<String, String> envs = conf.getEnvs();
                    //FIELD_CLINET_IP
                    //FIELD_CLINET_PORT
                    //FIELD_NC_HOME
                    envs.put("FIELD_CLINET_IP", ProjectNCConfigUtil.getNCClientIP());
                    envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
                    envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
                    conf.setEnvs(envs);
                    conf.setWorkingDirectory(ncHome.getPath());

                    if (conf.getBeforeRunTasks() != null) {
                        conf.setBeforeRunTasks(new ArrayList<>(conf.getBeforeRunTasks()));
                    } else {
                        conf.setBeforeRunTasks(new ArrayList<>());
                    }
                    conf.getBeforeRunTasks().add(new CompileStepBeforeRun.MakeBeforeRunTask());
                }
            });
        }

        if (0 == hasNc[0]) {
            //新增服务端
            ApplicationConfiguration conf = new ApplicationConfiguration(
                    ProjectNCConfigUtil.getNCVersion() + "服务端"
                    , project,
                    ApplicationConfigurationType.getInstance());
            conf.setMainClassName(serverClass);

            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
            conf.setEnvs(envs);

            conf.setVMParameters(
                    " -Dnc.http.port=$FIELD_CLINET_PORT$ "
                            + " -Dcom.sun.management.jmxremote "
                            //  + " -Dcom.sun.management.jmxremote.port=" + RandomUtil.randomInt(25000, 55000)
                            + " -Dcom.sun.management.jmxremote.ssl=false "
                            + " -Dcom.sun.management.jmxremote.authenticate=false "
                            + " -Dnc.exclude.modules=" //${FIELD_EX_MODULES}
                            + " -Dnc.runMode=develop "
                            + " -Dnc.server.location=$FIELD_NC_HOME$ "
                            + " -Dorg.owasp.esapi.resources=$FIELD_NC_HOME$/ierp/bin/esapi "
                            + " -DEJBConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -DExtServiceConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -XX:+HeapDumpOnOutOfMemoryError "
                            + " -Duap.hotwebs=" + ProjectNCConfigUtil.getNcHotWebsList()
                            + " -Djava.awt.headless=true "
                            + " -Dlog4j.ignoreTCL=true "
                            + " -Duser.timezone=GMT+8 "
                            + (
                            NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion()) ?
                                    " -Dfile.encoding=UTF-8 "
                                    : (
                                    " -Xmx1024m -XX:MaxPermSize=256m "
                                            + " -Dfile.encoding=GBK "
                            )
                    )
                            // + " -Xdebug -Xrunjdwp:transport=dt_socket,address=" + RandomUtil.randomInt(25000,
                            // 55000) + ",server=y,suspend=n "
                          /*  + " -Dnc.server.name= "
                            + " -Dnc.server.startCount=0 "
                            + " -Dnc.bs.logging.format=text "
                            + " -Drun.side=server "
                            + " -Dnc.run.side=server "*/
                            + " -Dnc.fi.autogenfile=N "
                            + " -Duap.disable.codescan=false "
                            + " -Dlocal.exclude.modules= "
            );
            conf.setWorkingDirectory(ncHome.getPath());
            conf.setModule(IdeaProjectGenerateUtil.getProjectModules(project)[0]);
            conf.setShowConsoleOnStdErr(true);
            conf.setShortenCommandLine(ShortenCommandLine.MANIFEST);

            if (conf.getBeforeRunTasks() != null) {
                conf.setBeforeRunTasks(new ArrayList<>(conf.getBeforeRunTasks()));
            } else {
                conf.setBeforeRunTasks(new ArrayList<>());
            }
            conf.getBeforeRunTasks().add(new CompileStepBeforeRun.MakeBeforeRunTask());

            RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf, true, true);
        }

        if (0 == hasNc[1]) {
            //新增客户端
            ApplicationConfiguration conf = new ApplicationConfiguration(
                    ProjectNCConfigUtil.getNCVersion() + "客户端", project,
                    ApplicationConfigurationType.getInstance());
            conf.setMainClassName(clientClass);
            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_CLINET_IP", ProjectNCConfigUtil.getNCClientIP());
            envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            conf.setEnvs(envs);

            conf.setVMParameters(
                    " -Dcom.sun.management.jmxremote "
                            //    + "-Dcom.sun.management.jmxremote.port=" + RandomUtil.randomInt(25000, 55000)
                            + " -Dcom.sun.management.jmxremote.ssl=false "
                            + "-Dcom.sun.management.jmxremote.authenticate=false "
                            + "-Dnc.runMode=develop"
                            + " -Dnc.jstart.server=$FIELD_CLINET_IP$"
                            + " -Dnc.jstart.port=$FIELD_CLINET_PORT$"
                            + " -Dnc.fi.autogenfile=N "
            );
            conf.setModule(IdeaProjectGenerateUtil.getProjectModules(project)[0]);
            conf.setWorkingDirectory(ncHome.getPath());
            conf.setShowConsoleOnStdErr(true);
            conf.setShortenCommandLine(ShortenCommandLine.MANIFEST);

            if (conf.getBeforeRunTasks() != null) {
                conf.setBeforeRunTasks(new ArrayList<>(conf.getBeforeRunTasks()));
            } else {
                conf.setBeforeRunTasks(new ArrayList<>());
            }
            conf.getBeforeRunTasks().add(new CompileStepBeforeRun.MakeBeforeRunTask());

            RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf, false, true);
        }
    }

    public static boolean checkNCHomeSetPass() {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            Messages.showInfoMessage("未配置NC HOME，请在 Tools 菜单下 配置NC HOME 菜单进行配置！", "警告");
            return false;
        }

        return true;
    }

    public static void updateApplicationNCLibrarys(@Nullable Project theProject) {
        updateApplicationNCLibrarys0(theProject);
    }

    /**
     * 更新用户配置的 项目的NC 路径更新 NC的库依赖
     */
    public static void updateApplicationNCLibrarys0(@Nullable Project theProject) {
        if (!checkNCHomeSetPass()) {
            return;
        }

        Project project = null == theProject ? ProjectUtil.getDefaultProject() : theProject;

        File ncHome = new File(ProjectNCConfigUtil.getNCHomePath());

        if (!ncHome.exists() && !ncHome.isDirectory()) {
            Messages.showInfoMessage("NC HOME不正确，请在 Tools 菜单下 配置NC HOME 菜单进行配置！", "警告");
        }
        ArrayList<LibraryEx> LibraryExList = new ArrayList<>();
        Boolean finalsh = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                try {
                    if (NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVersion())) {
                        File jar = new File(ncHome, "external" + File.separatorChar + "lib" + File.separatorChar +
                                "xerces.jar");
                        if (jar.isFile()) {
                            LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                                    ProjectNCConfigUtil.U8C_RUN_FIRST_DEPEND
                                    , CollUtil.toList(jar)));
                        }
                    }

                    // 是否为行业版
                    boolean hyVersion = ProjectNCConfigUtil.isHyVersion(project);

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Ant_Library
                            , IoUtil.serachAllNcAntJars(ncHome)));

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Middleware_Library
                            , IoUtil.serachMiddleware_LibraryJars(ncHome)));

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Framework_Library
                            , IoUtil.serachFramework_LibraryJars(ncHome)));

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Product_Common_Library
                            , IoUtil.serachProduct_Common_LibraryJars(ncHome)));

                    if (hyVersion) {
                        LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                                ProjectNCConfigUtil.LIB_NC_Module_Public_Hyext_Library
                                , IoUtil.serachNC_Module_Public_Hyext_Library(ncHome)));
                    }

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_NC_Module_Public_Library
                            , IoUtil.serachNC_Module_Public_Library(ncHome)));

                    if (hyVersion) {
                        LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                                ProjectNCConfigUtil.LIB_Module_Client_Hyext_Library
                                , IoUtil.serachModule_Client_Hyext_Library(ncHome)));
                    }

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Module_Client_Library
                            , IoUtil.serachModule_Client_Library(ncHome)));

                    ArrayList<File> extraJars = IoUtil.serachModule_Private_Extra_Library(ncHome);
                    if (!extraJars.isEmpty()) {
                        LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                                ProjectNCConfigUtil.LIB_Module_Private_Extra_Library
                                , extraJars));
                    }

                    if (hyVersion) {
                        LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                                ProjectNCConfigUtil.LIB_Module_Private_Hyext_Library
                                , IoUtil.serachModule_Private_Hyext_Library(ncHome)));
                    }

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Module_Private_Library
                            , IoUtil.serachModule_Private_Library(ncHome)));


                    //hotwebs系列
                    List<File> hotwebs = ApplicationLibraryUtil.getHotwebsModules(project);
                    if (!hotwebs.isEmpty()) {
                        for (File h : hotwebs) {
                            LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project
                                    , StringUtil.format("NC_LIBS/hotwebs_%s", h.getName())
                                    , IoUtil.serachHotwebs_Library(ncHome, h.getName())));
                        }
                    }

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Module_Lang_Library
                            , IoUtil.serachModule_Lang_Library(ncHome)));

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_Generated_EJB
                            , IoUtil.serachGenerated_EJB(ncHome)));

                    LibraryExList.add(ApplicationLibraryUtil.addApplicationLibrary(project,
                            ProjectNCConfigUtil.LIB_RESOURCES
                            , IoUtil.serachResources(ncHome)));
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error("更新NC依赖库失败:" + e.getMessage(), e);
                } finally {
                }

                return true;
            }
        });

        finalsh = ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                try {
                    // 向项目模块依赖中增加新增的库
                    Module[] modules = IdeaProjectGenerateUtil.getProjectModules(project);
                    for (Module module : modules) {
                        for (LibraryEx library : LibraryExList) {
                            ApplicationLibraryUtil.removeLib(module,
                                    library.getName());

                            ModuleRootModificationUtil.addDependency(module, library,
                                    ProjectNCConfigUtil.getLibScope(module), false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error("更新NC依赖库失败:" + e.getMessage(), e);
                }

                return true;
            }
        });
    }

    /**
     * <br>
     * 马上把最新的项目里所有模块的 META-INF 里所有文件复制到(包括ncc项目的hotwebs的文件)
     * NC HOME里对应的项目模块文件夹，主要是 ejb部署xml       <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 15:04
     * @Param []
     */
    public static void copyProjectMetaInfFiles2NCHomeModules() {
        Project project = ProjectUtil.getDefaultProject();
        Module[] modules = IdeaProjectGenerateUtil.getProjectModules(project);

        ExportContentVO contentVO = new ExportContentVO();
        contentVO.setProject(project);
        AbstractContentSearchImpl.initModules(contentVO);
        contentVO.initSelectModules();

        for (Module module : modules) {
            LogUtil.tryInfo(module.getModuleFilePath()
                    + " 模块执行：NC 各种配置文件 复制到NCHOME:"
                    + ProjectNCConfigUtil.getNCHomePath());
            copyProjectMetaInfFiles2NCHomeModules(contentVO, module);
        }
    }

    /**
     * <br>
     * 马上把指定模块的 META-INF 里所有文件复制到
     * NC HOME里对应的项目模块文件夹，主要是 ejb部署xml       <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 15:04
     * @Param []
     */
    public static void copyProjectMetaInfFiles2NCHomeModules(ExportContentVO contentVO, @NotNull Module module) {
        try {
            copyModuleMetainfoDir2NChome(module);

            if (NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion())) {
                copyModuleNccConfigDir2NChome(module);
                if (!"true".equals(ProjectNCConfigUtil.getConfigValue("close_client_copy"))) {
                    try {
                        copyModuleNccActionDir2NChome(contentVO, module);
                    } catch (Throwable e) {
                        LogUtil.error(e.toString(), e);
                    }
                } else {
                    LogUtil.tryInfo(module.getModuleFilePath()
                            + " 模块执行：NCC-hotwebs文件复制到NCHOME. 配置了跳过。执行跳过。");
                }
            }
        } catch (Throwable e) {
            LogUtil.error(e.toString(), e);
        }
    }

    private static void copyModuleMetainfoDir2NChome(@NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        File umpDir = new File(new File(module.getModuleFilePath()).getParentFile(), "META-INF");
        if (!umpDir.exists()) {
            return;
        }

        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            return;
        }

        File modeluUmpDir = new File(ncHomePath, File.separatorChar + "modules"
                + File.separatorChar + module.getName() + File.separatorChar + "META-INF");
        if (!modeluUmpDir.exists() || !modeluUmpDir.isDirectory()) {
            modeluUmpDir.mkdirs();
        }

        //复制 ump 文件到这里
        File[] projectFiles = umpDir.listFiles(f -> f.isFile());
        Stream.of(projectFiles).forEach(f -> {
            try {
                //判断文件是否改变， 不改变的跳过复制
                File tof = new File(modeluUmpDir, f.getName());
                if (IoUtil.isNoChange(f, tof)) {
                    return;
                }
                Files.copy(f.toPath(), tof.toPath(), StandardCopyOption
                        .REPLACE_EXISTING);
            } catch (Exception e) {
            }
        });
    }

    /**
     * 复制NCC的配置文件：client
     *
     * @param module
     */
    private static void copyModuleNccConfigDir2NChome(@NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        File clientDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                + File.separatorChar + "client");
        if (!clientDir.exists()) {
            return;
        }

        File yyconfigDir = new File(clientDir, "yyconfig");
        if (!yyconfigDir.exists()) {
            return;
        }

        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            return;
        }

        // hotwebs\nccloud\WEB-INF\extend\yyconfig\modules\recr
        File ncHomeyyconfigDir = new File(ncHomePath, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "extend"
                + File.separatorChar + "yyconfig"
        );
        if (!ncHomeyyconfigDir.exists() || !ncHomeyyconfigDir.isDirectory()) {
            ncHomeyyconfigDir.mkdirs();
        }

        //复制 ump 文件到这里
        LogUtil.tryInfo("开始复制：NCC-hotwebs yyconfig文件复制到NCHOME: " + yyconfigDir.getPath());
        List<File> fs = IoUtil.getAllFiles(yyconfigDir, true);
        for (File f : fs) {
            try {
                File tof = IoUtil.replaceDir(f, yyconfigDir, ncHomeyyconfigDir);
                if (!tof.getParentFile().isDirectory()) {
                    tof.getParentFile().mkdirs();
                }

                LogUtil.tryInfo(module.getModuleFilePath()
                        + " 模块执行：NCC-hotwebs yyconfig文件复制到NCHOME: "
                        + f.getPath() + "   ->  " + tof.getPath());

                Files.copy(f.toPath()
                        , tof.toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 复制NCC的client action代码到hotwebs
     *
     * @param module
     */
    private static void copyModuleNccActionDir2NChome(ExportContentVO contentVO, @NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        ModuleWarpVO moduleWarpVO = new ModuleWarpVO(module);
        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            LogUtil.tryInfo("NCHOME无法找到，跳过NCC配置文件复制.");
            return;
        }

        // hotwebs\nccloud\WEB-INF\extend\yyconfig\modules\recr
        File nccClientClassDir = new File(ncHomePath, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "classes"
        );
        if (!nccClientClassDir.exists() || !nccClientClassDir.isDirectory()) {
            nccClientClassDir.mkdirs();
        }

        //复制
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        File classBaseDirFile = null;
        try {
            classBaseDirFile = new File(CompilerModuleExtension.getInstance(module).getCompilerOutputPath().getPath());
        } catch (Exception e) {
        }

        File outPathRoot = nccClientClassDir;
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);
        if (classBaseDirFile != null && configVO != null) {
            File clientDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                    + File.separatorChar + "client"
            );
            if (configVO.clientToModuleHotwebs && clientDir.exists()) {
                searchJavas(AbstractContentSearchImpl.NC_TYPE_CLIENT, contentVO
                        , moduleWarpVO
                        , nccClientClassDir
                        , classBaseDirFile
                        , outPathRoot
                        , clientDir
                        , classBaseDirFile.getPath());
            }

            File publicDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                    + File.separatorChar + "public"
            );
            if (configVO.publicToModuleHotwebs && publicDir.exists()) {
                searchJavas(AbstractContentSearchImpl.NC_TYPE_PUBLIC, contentVO
                        , moduleWarpVO
                        , nccClientClassDir
                        , classBaseDirFile
                        , outPathRoot
                        , publicDir
                        , classBaseDirFile.getPath());
            }

            File privateDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                    + File.separatorChar + "private"
            );
            if (configVO.privateToModuleHotwebs && privateDir.exists()) {
                searchJavas(AbstractContentSearchImpl.NC_TYPE_PUBLIC, contentVO
                        , moduleWarpVO
                        , nccClientClassDir
                        , classBaseDirFile
                        , outPathRoot
                        , privateDir
                        , classBaseDirFile.getPath());
            }
        }

        SimpleCopyOutPutFileImpl executor = new SimpleCopyOutPutFileImpl();
        AbstarctBeforRule beforRule = new EmptyBeforRule();
        beforRule.addNext(new HotwebsNccloudClass2NCHOMEBeforRule());
        for (FileContentVO outFile : contentVO.getOutFiles()) {
            if (outFile.getExecutor() == null) {
                outFile.setExecutor(executor);
            }

            outFile.getExecutor().outPut(contentVO, outFile, beforRule, null);
        }
    }

    public static Module[] getProjectModules(Project project) {
        if (project == null) {
            LogUtil.infoAndHide("项目为空，无法获取到模块！！！！");
            return null;
        }

        Module[] modules = null;

        try {
            modules = ModuleManager.getInstance(project).getSortedModules();

            if (CollUtil.isEmpty(modules)) {
                LogUtil.infoAndHide("项目无法获取到模块！！！！" + project.getBasePath());
            }
        } catch (Throwable e) {
            modules = ModuleManager.getInstance(project).getModules();
            if (CollUtil.isEmpty(modules)) {
                LogUtil.infoAndHide("项目无法获取到模块2！！！！" + project.getBasePath());
            }
        }

        return modules;
    }

    public static String getModuleBaseDir(Module module) {
        if (module == null) {
            return null;
        }

        String path = module.getModuleFile().getParent().getPath();
        String projectPath = module.getProject().getBasePath();

        path = StrUtil.replace(path, "\\", File.separator);
        path = StrUtil.replace(path, "/", File.separator);

        projectPath = StrUtil.replace(projectPath, "\\", File.separator);
        projectPath = StrUtil.replace(projectPath, "/", File.separator);

        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        if (!projectPath.endsWith(File.separator)) {
            projectPath += File.separator;
        }

        if ((projectPath + ".idea" + File.separator).equals(path)) {
            return module.getProject().getBasePath();
        }

        return module.getModuleFile().getParent().getPath();
    }

    private static void searchJavas(String ncType
            , ExportContentVO contentVO
            , ModuleWarpVO moduleWarpVO
            , File nccClientClassDir
            , File classBaseDirFile
            , File outPathRoot
            , File clientDir
            , String classDir) {
        //获取所有的 有源码的包路径文件夹！
        List<File> allSourcePackges = IoUtil.getAllLastPackges(clientDir);
        for (final File sourcePackge : allSourcePackges) {
            String packgePath = sourcePackge.getPath().substring(clientDir.getPath().length());
            //class文件位置
            File classFileDir = new File(classBaseDirFile, packgePath);
            LogUtil.tryInfo("复制" + ncType + "代码文件夹: " + nccClientClassDir.getPath());
            ModuleJavaFileContentSearchImpl moduleJavaFileContentSearch = new ModuleJavaClientFileContentSearchImpl();
            moduleJavaFileContentSearch.copyClassAndJavaSourceFiles(sourcePackge, clientDir
                    , contentVO, outPathRoot.getPath(), moduleWarpVO
                    , ncType, packgePath, classFileDir, classDir);

            if (!StringUtil.replaceAll(StringUtil.replaceAll(sourcePackge.getPath(), File.separator, "/"), "\\",
                    "/").contains("client/yyconfig/")) {
                moduleJavaFileContentSearch.copyClassPathOtherFile(sourcePackge, outPathRoot.getPath(), moduleWarpVO
                        , ncType, packgePath, contentVO, classDir);
            }
        }
    }

    /**
     * 部署编译后的文件到 nchome中 hotwebs的代码里
     */
    public static class HotwebsNccloudClass2NCHOMEBeforRule extends AbstarctBeforRule {
        @Override
        public void doBefor(ExportContentVO contentVO, FileContentVO fileContentVO) {
            if (fileContentVO.getModule() == null) {
                return;
            }
            ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(fileContentVO.getModule().getModule());
            File hotwebs = new File(ProjectNCConfigUtil.getNCHome(contentVO.getProject()), "hotwebs");
            if (configVO.clientToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_CLIENT.equals(fileContentVO.getName())) {
                if (StringUtil.isAllNotBlank(fileContentVO.getFile(), fileContentVO.getFileTo())) {
                    fileContentVO.setFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getFile(), fileContentVO.getSrcTop())
                    );
                }
                if (StringUtil.isAllNotBlank(fileContentVO.getSrcFile(), fileContentVO.getSrcFileTo())) {
                    fileContentVO.setSrcFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getSrcFile(), fileContentVO.getSrcTop())
                    );
                }

                return;
            }

            if (configVO.privateToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_PRIVATE.equals(fileContentVO.getName())) {
                if (StringUtil.isAllNotBlank(fileContentVO.getFile(), fileContentVO.getFileTo())) {
                    fileContentVO.setFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getFile(), fileContentVO.getSrcTop())
                    );
                }
                if (StringUtil.isAllNotBlank(fileContentVO.getSrcFile(), fileContentVO.getSrcFileTo())) {
                    fileContentVO.setSrcFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getSrcFile(), fileContentVO.getSrcTop())
                    );
                }
                return;
            }

            if (configVO.publicToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_PUBLIC.equals(fileContentVO.getName())) {
                if (StringUtil.isAllNotBlank(fileContentVO.getFile(), fileContentVO.getFileTo())) {
                    fileContentVO.setFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getFile(), fileContentVO.getSrcTop())
                    );
                }
                if (StringUtil.isAllNotBlank(fileContentVO.getSrcFile(), fileContentVO.getSrcFileTo())) {
                    fileContentVO.setSrcFileTo(hotwebs.getPath()
                            + File.separatorChar + configVO.getModuleHotwebsName()
                            + File.separatorChar + "WEB-INF"
                            + File.separatorChar + "classes"
                            + IoUtil.rigthPathRemovePrefix(fileContentVO.getSrcFile(), fileContentVO.getSrcTop())
                    );
                }
                return;
            }
        }
    }

    private IdeaProjectGenerateUtil() {
    }

    /**
     * 设置 新模块的 源码等文件夹 结构
     *
     * @param module
     * @commot file://
     * @see ModulesConfigurator#apply()
     */
    public static void setModuleStructureConfigurable(Module module) {
        //这里必须 ！防止文件还没出现！！！！！！！
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

        ModifiableModuleModel modifiableModel = ModuleManager.getInstance(module.getProject()).getModifiableModel();
        ModulesConfigurator modulesConfigurator = new ModulesConfigurator(module.getProject(),
                ProjectStructureConfigurable.getInstance(module.getProject()));
        ModuleEditor editor = modulesConfigurator.getOrCreateModuleEditor(module);
        ContentEntry[] contentEntries = editor.getModifiableRootModel().getContentEntries();
        ContentEntry contentEntry = contentEntries[0];
        VirtualFile root = contentEntry.getRootModel().getContentRoots()[0];
        VirtualFile f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/src/public");
        if (f != null) {
            contentEntry.addSourceFolder(f, false);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/src/client");
        if (f != null) {
            contentEntry.addSourceFolder(f, false);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/src/private");
        if (f != null) {
            contentEntry.addSourceFolder(f, false);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/src/test");
        if (f != null) {
            contentEntry.addSourceFolder(f, true);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/resources");
        if (f != null) {
            contentEntry.addSourceFolder(f, JavaResourceRootType.RESOURCE);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/METADATA");
        if (f != null) {
            contentEntry.addSourceFolder(f, JavaResourceRootType.RESOURCE);
        }
        f = VirtualFileManager.getInstance().refreshAndFindFileByUrl(root.getUrl() + "/META-INF");
        if (f != null) {
            contentEntry.addSourceFolder(f, JavaResourceRootType.RESOURCE);
        }

        SourceFolder[] sfs = contentEntry.getSourceFolders();
        for (SourceFolder sf : sfs) {
            if (sf.getUrl().endsWith("src")) {
                contentEntry.removeSourceFolder(sf);
            }
        }

        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                ModifiableModelCommitter.multiCommit(new ModifiableRootModel[]{editor.apply()}, modifiableModel);
            } catch (ConfigurationException e) {
                e.printStackTrace();
                LogUtil.error("新模块自动配置结构失败:" + e.getMessage(), e);
            }
        });
    }
}
