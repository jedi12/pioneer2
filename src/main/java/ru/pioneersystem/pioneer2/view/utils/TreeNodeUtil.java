package ru.pioneersystem.pioneer2.view.utils;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import ru.pioneersystem.pioneer2.model.Part;

import java.util.LinkedList;
import java.util.List;

public class TreeNodeUtil {
    public static final String FOLDER_TYPE = "folder";
    public static final String DOCUMENT_TYPE = "document";

    public static TreeNode toTree(List<Part> parts) {
        int level = 0;
        List<DefaultTreeNode> nodes = new LinkedList<>();
        List<DefaultTreeNode> rootNodes = new LinkedList<>();

        for (Part part : parts) {
            if (level != part.getTreeLevel()) {
                level = part.getTreeLevel();
                rootNodes = nodes;
                nodes = new LinkedList<>();
            }

            DefaultTreeNode node = new DefaultTreeNode(FOLDER_TYPE, part, null);

            for (DefaultTreeNode childNode: rootNodes) {
                if (part.getId() == ((Part) childNode.getData()).getParent()) {
                    node.getChildren().add(childNode);
                }
            }

            nodes.add(node);
        }

        Part rootPart = new Part();
        rootPart.setId(0);
        rootPart.setName("root");
        rootPart.setState(Part.State.SYSTEM);
        rootPart.setParent(0);
        rootPart.setTreeLevel(0);
        rootPart.setOwnerGroup(0);

        TreeNode root = new DefaultTreeNode(rootPart);
        root.getChildren().addAll(nodes);

        return root;
    }

    public static List<Part> toList(TreeNode treeNode, List<Part> partList, int treeLevelOffset) {

        if (partList.isEmpty()) {
            Part part = (Part) treeNode.getData();
            part.setTreeLevel(part.getTreeLevel() + treeLevelOffset);
            partList.add(part);
        }

        for (TreeNode childNode: treeNode.getChildren()) {
            Part subPart = (Part) childNode.getData();
            subPart.setTreeLevel(subPart.getTreeLevel() + treeLevelOffset);
            partList.add(subPart);
            toList(childNode, partList, treeLevelOffset);
        }
        return partList;
    }

    public static Part setTreeLevel(Part currPart, List<Part> partList) {
        currPart.setTreeLevel(1);
        for (Part part : partList) {
            if (currPart.getParent() == part.getId()) {
                currPart.setTreeLevel(part.getTreeLevel() + 1);
                break;
            }
        }
        return currPart;
    }
}