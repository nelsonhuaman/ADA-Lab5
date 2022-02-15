import java.util.Arrays;
import java.util.Comparator;

public class BTree<E extends Comparable<? super E>> implements BTreeInterface<E> {
	class Node<E extends Comparable<? super E>> {

        private E[] data = null;
        private int kSize = 0;
        private Node<E>[] chil = null;
        private int tChild = 0;

        protected Node<E> parent = null;

        private Node(Node<E> parent, int maxKSize, int maxChiSize) {
            this.parent = parent;
            this.data = (E[]) new Comparable[maxKSize + 1];
            this.kSize = 0;
            this.chil = new Node[maxChiSize + 1];
            this.tChild = 0;
        }
        
        private Comparator<Node<E>> comparator = new Comparator<Node<E>>() {
            @Override
            public int compare(Node<E> arg0, Node<E> arg1) {
                return arg0.getData(0).compareTo(arg1.getData(0));
            }
        };
        
        private E getData(int index) {
            return data[index];
        }

        private int indexOf(E value) {
            for (int i = 0; i < kSize; i++) {
                if (data[i].equals(value)) {
                    return i;
                }
            }
            return -1;
        }

        private void addKey(E value) {
            data[kSize++] = value;
            Arrays.sort(data, 0, kSize);
        }

        private E removeK(E value) {
            E removed = null;
            boolean found = false;
            if (kSize == 0) {
                return null;
            }
            for (int i = 0; i < kSize; i++) {
                if (data[i].equals(value)) {
                    found = true;
                    removed = data[i];
                } else if (found) {
                    // shift the rest of the keys down
                    data[i - 1] = data[i];
                }
            }
            if (found) {
                kSize--;
                data[kSize] = null;
            }
            return removed;
        }

        private E removeK(int index) {
            if (index >= kSize) {
                return null;
            }
            E value = data[index];
            for (int i = index + 1; i < kSize; i++) {
                // shift the rest of the keys down
                data[i - 1] = data[i];
            }
            kSize--;
            data[kSize] = null;
            return value;
        }

        private int numberOfK() {
            return kSize;
        }

        private Node<E> getChild(int index) {
            if (index >= tChild) {
                return null;
            }
            return chil[index];
        }

        private int indexOf(Node<E> child) {
            for (int i = 0; i < tChild; i++) {
                if (chil[i].equals(child)) {
                    return i;
                }
            }
            return -1;
        }

        private boolean addChild(Node<E> child) {
            child.parent = this;
            chil[tChild++] = child;
            Arrays.sort(chil, 0, tChild, comparator);
            return true;
        }

        private boolean removeChild(Node<E> child) {
            boolean found = false;
            if (tChild == 0) {
                return found;
            }
            for (int i = 0; i < tChild; i++) {
                if (chil[i].equals(child)) {
                    found = true;
                } else if (found) {
                    // shift the rest of the keys down
                    chil[i - 1] = chil[i];
                }
            }
            if (found) {
            	tChild--;
                chil[tChild] = null;
            }
            return found;
        }

        private Node<E> removeChild(int index) {
            if (index >= tChild) {
                return null;
            }
            Node<E> value = chil[index];
            chil[index] = null;
            for (int i = index + 1; i < tChild; i++) {
                chil[i - 1] = chil[i];
            }
            tChild--;
            chil[tChild] = null;
            return value;
        }

        private int numChild() {
            return tChild;
        }
    }
    
    private int minKSize;
    private int minChildSize;
    private int maxKSize;
    private int maxChildSize;

    private Node<E> root = null;
    private int size = 0;

    public BTree() {
        this.maxKSize = 1;
        this.minChildSize = this.minKSize + 1;
        this.maxKSize = 2 * this.maxKSize;
        this.maxChildSize = this.maxKSize + 1;
    }

    @Override
    public boolean add(E value) {
        if (root == null) {
            root = new Node<E>(null, maxKSize, maxChildSize);
            root.addKey(value);
        } else {
            Node<E> node = root;
            while (node != null) {
                if (node.numChild() == 0) {
                    node.addKey(value);
                    if (node.numberOfK() <= maxKSize) {
                        break;
                    }
                    split(node);
                    break;
                }

                E min = node.getData(0);
                if (value.compareTo(min) <= 0) {
                    node = node.getChild(0);
                    continue;
                }
                int numberOfKeys = node.numberOfK();
                int last = numberOfKeys - 1;
                E greater = node.getData(last);
                if (value.compareTo(greater) > 0) {
                    node = node.getChild(numberOfKeys);
                    continue;
                }
                for (int i = 1; i < node.numberOfK(); i++) {
                    E prev = node.getData(i - 1);
                    E next = node.getData(i);
                    if (value.compareTo(prev) > 0 && value.compareTo(next) <= 0) {
                        node = node.getChild(i);
                        break;
                    }
                }
            }
        }

        size++;

        return true;
    }
    
    private void split(Node<E> nodeToSplit) {
        Node<E> node = nodeToSplit;
        int numberOfKeys = node.numberOfK();
        int medianIndex = numberOfKeys / 2;
        E medianValue = node.getData(medianIndex);

        Node<E> left = new Node<E>(null, maxKSize, maxChildSize);
        for (int i = 0; i < medianIndex; i++) {
            left.addKey(node.getData(i));
        }
        if (node.numChild() > 0) {
            for (int j = 0; j <= medianIndex; j++) {
                Node<E> c = node.getChild(j);
                left.addChild(c);
            }
        }

        Node<E> right = new Node<E>(null, maxKSize, maxChildSize);
        for (int i = medianIndex + 1; i < numberOfKeys; i++) {
            right.addKey(node.getData(i));
        }
        if (node.numChild() > 0) {
            for (int j = medianIndex + 1; j < node.numChild(); j++) {
                Node<E> c = node.getChild(j);
                right.addChild(c);
            }
        }

        if (node.parent == null) {
            Node<E> newRoot = new Node<E>(null, maxKSize, maxChildSize);
            newRoot.addKey(medianValue);
            node.parent = newRoot;
            root = newRoot;
            node = root;
            node.addChild(left);
            node.addChild(right);
        } else {
            Node<E> parent = node.parent;
            parent.addKey(medianValue);
            parent.removeChild(node);
            parent.addChild(left);
            parent.addChild(right);

            if (parent.numberOfK() > maxKSize) {
                split(parent);
            }
        }
    }

    @Override
    public E remove(E value) {
        E removed = null;
        Node<E> node = this.getNode(value);
        removed = remove(value, node);
        return removed;
    }
    
    private E remove(E value, Node<E> node) {
        if (node == null) {
            return null;
        }

        E removed = null;
        int index = node.indexOf(value);
        removed = node.removeK(value);
        if (node.numChild() == 0) {
            if (node.parent != null && node.numberOfK() < minKSize) {
                this.joint(node);
            } else if (node.parent == null && node.numberOfK() == 0) {
                root = null;
            }
        } else {
            Node<E> min = node.getChild(index);
            Node<E> aux = this.getUpdate(min);
            E replaceValue = this.removeValue(aux);
            node.addKey(replaceValue);
            if (aux.parent != null && aux.numberOfK() < minKSize) {
                this.joint(aux);
            }
            if (aux.numChild() > maxChildSize) {
                this.split(aux);
            }
        }
        size--;
        return removed;
    }

    private boolean joint(Node<E> node) {
        Node<E> parent = node.parent;
        int index = parent.indexOf(node);
        int indexOfLeftNeighbor = index - 1;
        int indexOfRightNeighbor = index + 1;

        Node<E> right = null;
        int rightNeighborSize = -minChildSize;
        if (indexOfRightNeighbor < parent.numChild()) {
            right = parent.getChild(indexOfRightNeighbor);
            rightNeighborSize = right.numberOfK();
        }

        if (right != null && rightNeighborSize > minKSize) {
            E removeValue = right.getData(0);
            int prev = getPrevious(parent, removeValue);
            E parentValue = parent.removeK(prev);
            E neighborValue = right.removeK(0);
            node.addKey(parentValue);
            parent.addKey(neighborValue);
            if (right.numChild() > 0) {
                node.addChild(right.removeChild(0));
            }
        } else {
            Node<E> left = null;
            int leftNeighborSize = -minChildSize;
            if (indexOfLeftNeighbor >= 0) {
                left = parent.getChild(indexOfLeftNeighbor);
                leftNeighborSize = left.numberOfK();
            }

            if (left != null && leftNeighborSize > minKSize) {
                E removeValue = left.getData(left.numberOfK() - 1);
                int prev = getNext(parent, removeValue);
                E parentValue = parent.removeK(prev);
                E value = left.removeK(left.numberOfK() - 1);
                node.addKey(parentValue);
                parent.addKey(value);
                if (left.numChild() > 0) {
                    node.addChild(left.removeChild(left.numChild() - 1));
                }
            } else if (right != null && parent.numberOfK() > 0) {

                E removeValue = right.getData(0);
                int prev = getPrevious(parent, removeValue);
                E parentValue = parent.removeK(prev);
                parent.removeChild(right);
                node.addKey(parentValue);
                for (int i = 0; i < right.kSize; i++) {
                    E v = right.getData(i);
                    node.addKey(v);
                }
                for (int i = 0; i < right.tChild; i++) {
                    Node<E> c = right.getChild(i);
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfK() < minKSize) {

                    this.joint(parent);
                } else if (parent.numberOfK() == 0) {
                    node.parent = null;
                    root = node;
                }
            } else if (left != null && parent.numberOfK() > 0) {
                E removeValue = left.getData(left.numberOfK() - 1);
                int prev = getNext(parent, removeValue);
                E parentValue = parent.removeK(prev);
                parent.removeChild(left);
                node.addKey(parentValue);
                for (int i = 0; i < left.kSize; i++) {
                    E v = left.getData(i);
                    node.addKey(v);
                }
                for (int i = 0; i < left.tChild; i++) {
                    Node<E> c = left.getChild(i);
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfK() < minKSize) {
                    this.joint(parent);
                } else if (parent.numberOfK() == 0) {
                    node.parent = null;
                    root = node;
                }
            }
        }

        return true;
    }

    private E removeValue(Node<E> node) {
        E value = null;
        if (node.numberOfK() > 0) {
            value = node.removeK(node.numberOfK() - 1);
        }
        return value;
    }

    private int getNext(Node<E> node, E value) {
        for (int i = 0; i < node.numberOfK(); i++) {
            E t = node.getData(i);
            if (t.compareTo(value) >= 0) {
                return i;
            }
        }
        return node.numberOfK() - 1;
    }

    private Node<E> getUpdate(Node<E> Newnode) {
        Node<E> node = Newnode;
        while (node.numChild() > 0) {
            node = node.getChild(node.numChild() - 1);
        }
        return node;
    }

    private int getPrevious(Node<E> node, E value) {
        for (int i = 1; i < node.numberOfK(); i++) {
            E t = node.getData(i);
            if (t.compareTo(value) >= 0) {
                return i - 1;
            }
        }
        return node.numberOfK() - 1;
    }

    private Node<E> getNode(E value) {
        Node<E> node = root;
        while (node != null) {
            E min = node.getData(0);
            if (value.compareTo(min) < 0) {
                if (node.numChild() > 0) {
                    node = node.getChild(0);
                } else {
                    node = null;
                }
                continue;
            }

            int numberOfKeys = node.numberOfK();
            int last = numberOfKeys - 1;
            E greater = node.getData(last);
            if (value.compareTo(greater) > 0) {
                if (node.numChild() > numberOfKeys) {
                    node = node.getChild(numberOfKeys);
                } else {
                    node = null;
                }
                continue;
            }

            for (int i = 0; i < numberOfKeys; i++) {
                E currentValue = node.getData(i);
                if (currentValue.compareTo(value) == 0) {
                    return node;
                }

                int next = i + 1;
                if (next <= last) {
                    E nextValue = node.getData(next);
                    if (currentValue.compareTo(value) < 0 && nextValue.compareTo(value) > 0) {
                        if (next < node.numChild()) {
                            node = node.getChild(next);
                            break;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean contains(E value) {
        Node<E> node = getNode(value);
        return (node != null);
    }

    @Override
    public int size() {
        return size;
    }
}
