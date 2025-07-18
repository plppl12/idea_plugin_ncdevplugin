package com.air.nc5dev.util.idea;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目 依赖路径管理 工具<br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 8:55
 * @project
 */
public class ApplicationLibraryUtil {


    /**
     * 往 IDEA 的项目 jar依赖增加一个 依赖库，如果依赖库已存在 就删除在增加
     *
     * @param theProject
     * @param libraryName
     * @param files
     * @return
     */
    public static LibraryEx addApplicationLibrary(@Nullable Project theProject, @NotNull String libraryName, @NotNull List<File> files) {
        Project project = null == theProject ? ProjectUtil.getDefaultProject() : theProject;
        LibraryTable.ModifiableModel model = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();

        LibraryEx library = (LibraryEx) model.getLibraryByName(libraryName);
        if (library != null) {
            // 已经存在库的，删除库里的东西
            //   model.removeLibrary(library);
        } else {
            library = (LibraryEx) model.createLibrary(libraryName);
        }

        LibraryEx.ModifiableModelEx libraryModel = library.getModifiableModel();

        // 参数转换成路径集合
        List<String> classesRoots = files.stream().map(File::getPath).collect(Collectors.toList());

        // 加入新的依赖路径
        for (String root : classesRoots) {
            if (root.toLowerCase().endsWith("_src.jar")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.SOURCES);
            } else if (root.toLowerCase().endsWith(".jar")) {
                // 注意jar格式jar:{path_to_jar}.jar!/
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.CLASSES);
            } else if (root.toLowerCase().endsWith(".class")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
            } else {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            }
        }

        // 提交库变更
        ApplicationManager.getApplication().runWriteAction(() -> {
            libraryModel.commit();
            model.commit();
        });

        return library;
    }

    public static OrderEntry removeLib(Module module, String name) {
        ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
        OrderEntry[] os = modifiableModel.getOrderEntries();
        if (os == null) {
            return null;
        }

        for (OrderEntry orderEntry : os) {
            if (orderEntry != null
                    && orderEntry instanceof LibraryOrderEntry
                    && name.equals(((LibraryOrderEntry) orderEntry).getLibraryName())) {
                modifiableModel.removeOrderEntry(orderEntry);
                modifiableModel.commit();
                return orderEntry;
            }
        }

        return null;
    }

    public static OrderEntry getLib(ModifiableRootModel modifiableModel, String name) {
        OrderEntry[] os = modifiableModel.getOrderEntries();
        if (os == null) {
            return null;
        }

        for (OrderEntry orderEntry : os) {
            if (orderEntry != null
                    && orderEntry instanceof LibraryOrderEntry
                    && name.equals(((LibraryOrderEntry) orderEntry).getLibraryName())) {
                return orderEntry;
            }
        }

        return null;
    }

    public static boolean notHas(ModifiableRootModel modifiableModel, String name) {
        OrderEntry[] os = modifiableModel.getOrderEntries();
        if (os == null) {
            return true;
        }

        for (OrderEntry orderEntry : os) {
            if (orderEntry != null
                    && orderEntry instanceof LibraryOrderEntry
                    && name.equals(((LibraryOrderEntry) orderEntry).getLibraryName())) {
                return false;
            }
        }

        return true;
    }


    private ApplicationLibraryUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }

    /**
     * 添加NC的依赖们到 模块
     *
     * @param module
     */
    public static void addLibs2Module(Module module) {
        //添加依赖！
        final LibraryTable.ModifiableModel model = LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject()).getModifiableModel();
        ArrayList<LibraryEx> LibraryExList = new ArrayList<>();
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Ant_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Middleware_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Framework_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Product_Common_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_NC_Module_Public_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Module_Client_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Module_Private_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Module_Lang_Library));
        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Generated_EJB));

        //hotwebs系列
        List<File> hotwebs = getHotwebsModules(module.getProject());
        if (!hotwebs.isEmpty()) {
            for (File h : hotwebs) {
                LibraryEx lb = (LibraryEx) model.getLibraryByName(StringUtil.format("NC/hotwebs_%s"
                        , h.getName()));
                if (lb == null) {
                    continue;
                }
                LibraryExList.add(lb);
            }
        }

        LibraryExList.add((LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_RESOURCES));

        //  Compile  Test Runtime Provided
        final DependencyScope dependencyScope = ProjectNCConfigUtil.getLibScope(module);

        for (LibraryEx library : LibraryExList) {
            if (library == null) {
                continue;
            }
            if (ApplicationLibraryUtil.notHas(ModuleRootManager.getInstance(module).getModifiableModel()
                    , library.getName())) {
                ModuleRootModificationUtil.addDependency(module, library, dependencyScope, false);
            }
        }
    }

    public static List<File> getHotwebsModules(Project project) {
        String home = ProjectNCConfigUtil.getNCHomePath(project);
        if (StringUtil.isBlank(home)) {
            return new ArrayList<>();
        }

        File h = new File(home);
        File hotwebs = new File(h, "hotwebs");
        if (!hotwebs.isDirectory()) {
            return new ArrayList<>();
        }

        File[] fs = hotwebs.listFiles();
        ArrayList<File> all = new ArrayList();

        for (File f : fs) {
            if (f.isDirectory()) {
                File wf = new File(f, "WEB-INF");
                if (!wf.isDirectory()) {
                    continue;
                }
                File lb = new File(wf, "lib");
                int i = 0;
                if (lb.isDirectory() && CollUtil.isEmpty(lb.listFiles())) {
                    for (File b : lb.listFiles()) {
                        if (b.isFile()) {
                            ++i;
                            break;
                        }
                    }
                }

                if (new File(wf, "classes").isDirectory()) {
                    ++i;
                }

                if (i < 1) {
                    continue;
                }

                all.add(f);
            }
        }

        return all;
    }
}
