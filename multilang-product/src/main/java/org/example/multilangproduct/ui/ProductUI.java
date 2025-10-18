package org.example.multilangproduct.ui;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import net.miginfocom.swing.MigLayout;
import org.example.multilangproduct.dao.LanguageDAO;
import org.example.multilangproduct.dao.ProductCategoryDAO;
import org.example.multilangproduct.dao.ProductDAO;
import org.example.multilangproduct.dto.ProductDisplayDTO;
import org.example.multilangproduct.entity.Language;
import org.example.multilangproduct.entity.Product;
import org.example.multilangproduct.entity.ProductCategory;
import org.example.multilangproduct.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

public class ProductUI extends JFrame {
    private final ProductService productService;
    private final ProductDAO productDAO;
    private final ProductCategoryDAO categoryDAO;
    private final LanguageDAO languageDAO;

    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField idField, enNameField, vnNameField, priceField;
    private JComboBox<String> categoryCombo;
    private Map<String, Integer> categoryMap;
    private JButton addButton, updateButton, deleteButton;
    private boolean isLoading = false;

    public ProductUI() {
        FlatMaterialLighterIJTheme.setup(); // 🌈 giao diện Material Design sáng
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        // ✨ THAY ĐỔI: Bật lại đường kẻ bảng để dễ phân biệt hơn
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("Table.showHorizontalLines", true);
        // ✨ MỚI: Tùy chỉnh màu của đường kẻ cho nhẹ nhàng
        UIManager.put("Table.gridColor", new Color(220, 220, 220));


        this.productService = new ProductService();
        this.productDAO = new ProductDAO();
        this.categoryDAO = new ProductCategoryDAO();
        this.languageDAO = new LanguageDAO();

        initializeUI();
        loadProducts();
    }

    private void initializeUI() {
        setTitle("📦 Quản lý Sản phẩm Đa Ngôn Ngữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // ====== BẢNG SẢN PHẨM ======
        String[] columns = {"ID", "Tên (EN)", "Tên (VN)", "Giá", "Danh mục (EN)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // ✨ THAY ĐỔI: Cột giá nên là BigDecimal hoặc Number để sắp xếp đúng
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return BigDecimal.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(30); // ✨ Tăng chiều cao dòng một chút
        productTable.setSelectionBackground(new Color(120, 180, 255));
        productTable.setIntercellSpacing(new Dimension(0, 0)); // ✨ Xóa khoảng cách thừa

        // ✨ MỚI: Căn giữa và làm đậm tiêu đề cột
        JTableHeader tableHeader = productTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 15)); // Tăng cỡ chữ và làm đậm
        tableHeader.setOpaque(false);
        tableHeader.setBackground(new Color(242, 242, 242));
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // ✨ MỚI: Căn giữa nội dung cho các cột ID và Giá
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Cột ID

        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer();
        priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT); // Căn phải cho giá đẹp hơn
        productTable.getColumnModel().getColumn(3).setCellRenderer(priceRenderer); // Cột Giá
        productTable.getColumnModel().getColumn(0).setMaxWidth(80); // Giới hạn chiều rộng cột ID


        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        JButton refreshButton = new JButton("Làm mới");
        refreshButton.addActionListener(e -> {
            if (!isLoading) {
                clearForm();
                loadCategoriesIntoCombo();
                loadProducts();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(scrollPane, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.CENTER);

        // ====== FORM NHẬP DỮ LIỆU ======
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, fillx", "[150][grow]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("📝 Thêm / Sửa Bản dịch"));

        idField = new JTextField();
        idField.setEnabled(false);
        priceField = new JTextField();
        enNameField = new JTextField();
        vnNameField = new JTextField();
        categoryCombo = new JComboBox<>();
        loadCategoriesIntoCombo();

        formPanel.add(new JLabel("Mã Sản Phẩm (ID):"));
        formPanel.add(idField, "growx");
        formPanel.add(new JLabel("Giá ($):"));
        formPanel.add(priceField, "growx");
        formPanel.add(new JLabel("Danh mục:"));
        formPanel.add(categoryCombo, "growx");
        formPanel.add(new JLabel("Tên (EN):"));
        formPanel.add(enNameField, "growx");
        formPanel.add(new JLabel("Tên (VN):"));
        formPanel.add(vnNameField, "growx");

        addButton = new JButton("Thêm Mới");
        updateButton = new JButton("Cập nhật");
        deleteButton = new JButton("Xóa");

        // ✨ MỚI: Thêm màu sắc cho các nút để sinh động và trực quan
        addButton.setBackground(new Color(46, 204, 113)); // Xanh lá
        addButton.setForeground(Color.WHITE);
        updateButton.setBackground(new Color(52, 152, 219)); // Xanh dương
        updateButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(231, 76, 60)); // Đỏ
        deleteButton.setForeground(Color.WHITE);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        btnPanel.add(addButton);
        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);

        formPanel.add(new JLabel(), "growx");
        formPanel.add(btnPanel, "growx");
        add(formPanel, BorderLayout.SOUTH);

        // ====== SỰ KIỆN ======
        addButton.addActionListener(new AddProductListener());
        updateButton.addActionListener(new UpdateProductListener());
        deleteButton.addActionListener(new DeleteProductListener());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                Integer productId = (Integer) tableModel.getValueAt(row, 0);
                Optional<Product> productOpt = productDAO.getProductById(productId);
                productOpt.ifPresent(p -> {
                    idField.setText(p.getProductId().toString());
                    priceField.setText(p.getPrice().toPlainString());
                    enNameField.setText((String) tableModel.getValueAt(row, 1));
                    vnNameField.setText((String) tableModel.getValueAt(row, 2));
                    categoryCombo.setSelectedItem(tableModel.getValueAt(row, 4));
                    // Khi chọn sản phẩm để sửa, không cho sửa giá và danh mục
                    priceField.setEnabled(false);
                    categoryCombo.setEnabled(false);
                });
            }
        });
    }

    // ====== LOAD DỮ LIỆU ======
    private void loadCategoriesIntoCombo() {
        categoryCombo.removeAllItems();
        categoryMap = new HashMap<>();
        try {
            Language enLang = languageDAO.getByCode("en");
            if (enLang == null) {
                categoryCombo.addItem("❌ Thiếu Ngôn ngữ EN");
                JOptionPane.showMessageDialog(this, "Vui lòng chạy MainApp để khởi tạo ngôn ngữ EN!");
                return;
            }

            List<ProductCategory> categories = categoryDAO.getAllCategories();
            for (ProductCategory cat : categories) {
                String name = categoryDAO.getCategoryNameByLanguage(cat.getProductCategoryId(), "en")
                        .orElse("Category " + cat.getProductCategoryId());
                categoryMap.put(name, cat.getProductCategoryId());
                categoryCombo.addItem(name);
            }
            if (categoryCombo.getItemCount() == 0)
                categoryCombo.addItem("⚠️ Chưa có Danh mục");
        } catch (Exception e) {
            e.printStackTrace();
            categoryCombo.addItem("⚠️ Lỗi load danh mục");
        }
    }

    private void loadProducts() {
        if (isLoading) return;
        isLoading = true;
        // ✨ Lưu lại dòng đang chọn
        int selectedRow = productTable.getSelectedRow();

        tableModel.setRowCount(0);
        try {
            List<ProductDisplayDTO> enList = productService.getProductsForDisplay("en");
            Map<Integer, String> vnMap = new HashMap<>();
            productService.getProductsForDisplay("vi")
                    .forEach(dto -> vnMap.put(dto.getProductId(), dto.getProductName()));

            for (ProductDisplayDTO dto : enList) {
                tableModel.addRow(new Object[]{
                        dto.getProductId(),
                        dto.getProductName(),
                        vnMap.getOrDefault(dto.getProductId(), "Chưa có tên VN"),
                        dto.getPrice(), // ✨ Giữ nguyên kiểu BigDecimal
                        dto.getCategoryName()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isLoading = false;
            // ✨ Khôi phục lại dòng đã chọn
            if (selectedRow != -1 && selectedRow < productTable.getRowCount()) {
                productTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }

    // ====== LISTENER CRUD ======
    private class AddProductListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String en = enNameField.getText().trim(), vn = vnNameField.getText().trim(), priceStr = priceField.getText().trim();
            if (en.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(ProductUI.this, "Vui lòng nhập tên EN và Giá!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                BigDecimal price = new BigDecimal(priceStr);
                String catName = (String) categoryCombo.getSelectedItem();
                if (catName == null || !categoryMap.containsKey(catName)) {
                    JOptionPane.showMessageDialog(ProductUI.this, "Danh mục không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ProductCategory cat = categoryDAO.getById(categoryMap.get(catName)).orElseThrow(() -> new IllegalStateException("Category not found"));
                Product p = new Product(price, BigDecimal.ZERO, cat);
                productDAO.saveProduct(p);
                productService.saveOrUpdateTranslation(p.getProductId(), "en", en, "EN description placeholder");
                if (!vn.isEmpty())
                    productService.saveOrUpdateTranslation(p.getProductId(), "vi", vn, "VN description placeholder");
                JOptionPane.showMessageDialog(ProductUI.this, "✅ Thêm sản phẩm thành công!");
                clearForm();
                loadProducts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ProductUI.this, "Giá phải là một con số!", "Lỗi Định dạng", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ProductUI.this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class UpdateProductListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (productTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(ProductUI.this, "Vui lòng chọn một sản phẩm để cập nhật!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Integer id = Integer.parseInt(idField.getText());
            try {
                productService.saveOrUpdateTranslation(id, "en", enNameField.getText(), "EN updated");
                productService.saveOrUpdateTranslation(id, "vi", vnNameField.getText(), "VN updated");
                JOptionPane.showMessageDialog(ProductUI.this, "💾 Cập nhật thành công!");
                loadProducts();
                clearForm();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ProductUI.this, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private class DeleteProductListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (productTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(ProductUI.this, "Vui lòng chọn một sản phẩm để xóa!", "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Integer id = Integer.parseInt(idField.getText());
            int confirmation = JOptionPane.showConfirmDialog(ProductUI.this, "Bạn có chắc chắn muốn xóa sản phẩm có ID " + id + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    productDAO.deleteProduct(id);
                    JOptionPane.showMessageDialog(ProductUI.this, "🗑️ Đã xóa sản phẩm thành công!");
                    loadProducts();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProductUI.this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    private void clearForm() {
        idField.setText("");
        enNameField.setText("");
        vnNameField.setText("");
        priceField.setText("");
        priceField.setEnabled(true);
        categoryCombo.setEnabled(true);
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        productTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductUI().setVisible(true));
    }
}