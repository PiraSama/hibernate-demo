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
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class ProductUI extends JFrame {
    private final ProductService productService;
    private final ProductDAO productDAO;
    private final ProductCategoryDAO categoryDAO;
    private final LanguageDAO languageDAO;

    private JTable productTable;
    private DefaultTableModel tableModel;

    private JTextField idField, priceField;
    private JComboBox<String> categoryCombo;
    private Map<String, Integer> categoryMap;
    private JButton addButton, updateButton, deleteButton;
    private boolean isLoading = false;

    // PHẦN MỚI CHO ĐA NGÔN NGỮ
    private JComboBox<String> languageCombo;
    // Map: Tên ngôn ngữ -> Mã ngôn ngữ
    private Map<String, String> langNameToCodeMap;
    private List<Language> allLanguages;
    private String currentLangCode = "en";

    // KHAI BÁO MỚI CHO TRƯỜNG DỊCH ĐỘNG
    private JPanel translationFieldsPanel;
    // Map: Mã ngôn ngữ -> JTextField
    private Map<String, JTextField> translationFieldsMap;

    public ProductUI() {
        FlatMaterialLighterIJTheme.setup(); // 🌈 giao diện Material Design sáng

        // ✨ SỬA FONT: Dùng 'Dialog' để đảm bảo hỗ trợ Unicode CJK (Tiếng Trung/Nhật) tốt hơn
        UIManager.put("defaultFont", new Font("Dialog", Font.PLAIN, 14));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.showVerticalLines", true);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.gridColor", new Color(220, 220, 220));


        this.productService = new ProductService();
        this.productDAO = new ProductDAO();
        this.categoryDAO = new ProductCategoryDAO();
        this.languageDAO = new LanguageDAO();

        // Tải tất cả ngôn ngữ một lần khi khởi tạo
        this.allLanguages = productService.getAllActiveLanguages();

        initializeUI();
        // Bắt đầu với ngôn ngữ mặc định là 'en'
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
        String[] columns = {"ID", "Tên Sản Phẩm", "Giá", "Danh mục"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 2) return BigDecimal.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Dialog", Font.PLAIN, 14)); // Đảm bảo bảng hiển thị đúng ký tự
        productTable.setSelectionBackground(new Color(120, 180, 255));
        productTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader tableHeader = productTable.getTableHeader();
        tableHeader.setFont(new Font("Dialog", Font.BOLD, 15));
        tableHeader.setOpaque(false);
        tableHeader.setBackground(new Color(242, 242, 242));
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        // ====== KHU VỰC ĐIỀU KHIỂN TRÊN CÙNG (CHUYỂN NGÔN NGỮ & REFRESH) ======
        languageCombo = new JComboBox<>();
        languageCombo.setFont(new Font("Dialog", Font.PLAIN, 14)); // Đảm bảo ComboBox hiển thị đúng ký tự CJK
        loadLanguagesIntoCombo();

        languageCombo.addActionListener(e -> {
            if (!isLoading && languageCombo.getSelectedItem() != null) {
                String langName = (String) languageCombo.getSelectedItem();
                // Lấy mã ngôn ngữ từ tên đã chọn
                currentLangCode = langNameToCodeMap.getOrDefault(langName, "en");
                loadProducts();
            }
        });

        JButton refreshButton = new JButton("Làm mới & Xóa Form");
        refreshButton.addActionListener(e -> {
            if (!isLoading) {
                clearForm();
                loadCategoriesIntoCombo();
                loadProducts();
            }
        });

        JPanel langRefreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        langRefreshPanel.add(new JLabel("Ngôn ngữ Hiển thị:"));
        langRefreshPanel.add(languageCombo);
        langRefreshPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(langRefreshPanel, BorderLayout.NORTH);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        add(topPanel, BorderLayout.CENTER);

        // ====== FORM NHẬP DỮ LIỆU ĐA NGÔN NGỮ ======
        JPanel formWrapperPanel = new JPanel(new MigLayout("wrap 2, fillx", "[150][grow]"));
        formWrapperPanel.setBorder(BorderFactory.createTitledBorder("📝 Quản lý Sản phẩm & Bản dịch"));

        idField = new JTextField();
        idField.setEnabled(false);
        priceField = new JTextField();
        categoryCombo = new JComboBox<>();
        loadCategoriesIntoCombo();

        // 1. CÁC TRƯỜNG CỐ ĐỊNH (ID, GIÁ, DANH MỤC)
        formWrapperPanel.add(new JLabel("Mã SP (ID):"));
        formWrapperPanel.add(idField, "growx");
        formWrapperPanel.add(new JLabel("Giá ($):"));
        formWrapperPanel.add(priceField, "growx");
        formWrapperPanel.add(new JLabel("Danh mục:"));
        formWrapperPanel.add(categoryCombo, "growx");

        // 2. PANEL CHỨA CÁC TRƯỜNG DỊCH THUẬT ĐỘNG
        translationFieldsPanel = new JPanel(new MigLayout("wrap 2, fillx", "[150][grow]"));
        JScrollPane translationScrollPane = new JScrollPane(translationFieldsPanel);
        translationScrollPane.setPreferredSize(new Dimension(400, 150));
        translationScrollPane.setBorder(BorderFactory.createTitledBorder("Bản dịch Tên Sản phẩm (Tất cả ngôn ngữ)"));

        formWrapperPanel.add(translationScrollPane, "span 2, growx, pushx");

        // Khởi tạo các trường nhập liệu bản dịch khi UI load
        createDynamicTranslationFields();


        // 3. CÁC NÚT HÀNH ĐỘNG
        addButton = new JButton("Thêm Mới");
        updateButton = new JButton("Cập nhật");
        deleteButton = new JButton("Xóa");

        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        updateButton.setBackground(new Color(52, 152, 219));
        updateButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(231, 76, 60));
        deleteButton.setForeground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        btnPanel.add(addButton);
        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);

        formWrapperPanel.add(new JLabel(), "growx");
        formWrapperPanel.add(btnPanel, "growx");
        add(formWrapperPanel, BorderLayout.SOUTH);

        // ====== SỰ KIỆN ======
        addButton.addActionListener(new AddProductListener());
        updateButton.addActionListener(new UpdateProductListener());
        deleteButton.addActionListener(new DeleteProductListener());

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                Integer productId = (Integer) tableModel.getValueAt(row, 0);
                loadProductDetailsToForm(productId);
            }
        });
    }

    // PHƯƠNG THỨC: Tải ngôn ngữ vào JComboBox
    private void loadLanguagesIntoCombo() {
        languageCombo.removeAllItems();
        langNameToCodeMap = new LinkedHashMap<>(); // Sửa: Dùng Map Tên -> Mã để tra cứu dễ hơn

        if (allLanguages.isEmpty()) {
            languageCombo.addItem("⚠️ Không có ngôn ngữ nào");
            return;
        }

        try {
            for (Language lang : allLanguages) {
                String langName = lang.getLangName();
                String langCode = lang.getLangCode();
                langNameToCodeMap.put(langName, langCode); // Lưu trữ Tên -> Mã
                languageCombo.addItem(langName); // Hiển thị Tên trong Combo
            }

            // Chọn ngôn ngữ mặc định (Tiếng Anh)
            String defaultLangName = allLanguages.stream()
                    .filter(lang -> lang.getLangCode().equals("en"))
                    .map(Language::getLangName)
                    .findFirst()
                    .orElse(null);

            if (defaultLangName != null) {
                languageCombo.setSelectedItem(defaultLangName);
            } else if (languageCombo.getItemCount() > 0) {
                languageCombo.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            languageCombo.addItem("⚠️ Lỗi tải ngôn ngữ");
        }
    }


    // PHƯƠNG THỨC MỚI: Tạo các JTextField động cho tất cả ngôn ngữ
    private void createDynamicTranslationFields() {
        translationFieldsPanel.removeAll();
        translationFieldsMap = new LinkedHashMap<>();

        // Lặp qua tất cả các ngôn ngữ đang hoạt động
        for (Language lang : allLanguages) {
            String langName = lang.getLangName();
            String langCode = lang.getLangCode();

            JTextField textField = new JTextField();

            // Đặt font rõ ràng cho trường nhập liệu để hiển thị CJK
            textField.setFont(new Font("Dialog", Font.PLAIN, 14));

            if (langCode.equals("en")) {
                textField.setToolTipText("Bản dịch Tiếng Anh là BẮT BUỘC");
            }

            translationFieldsPanel.add(new JLabel("Tên (" + langName + "):"));
            translationFieldsPanel.add(textField, "growx");
            translationFieldsMap.put(langCode, textField);
        }

        translationFieldsPanel.revalidate();
        translationFieldsPanel.repaint();
    }

    // PHƯƠNG THỨC MỚI: Tải chi tiết sản phẩm vào form (bao gồm bản dịch)
    private void loadProductDetailsToForm(Integer productId) {
        Optional<Product> productOpt = productDAO.getProductById(productId);
        if (productOpt.isEmpty()) return;

        Product p = productOpt.get();

        // Cập nhật các trường cố định
        idField.setText(p.getProductId().toString());
        priceField.setText(p.getPrice().toPlainString());

        // Khi chọn sản phẩm để sửa, không cho sửa giá và danh mục
        priceField.setEnabled(false);
        categoryCombo.setEnabled(false);

        // Cập nhật Danh mục (tên category lấy theo ngôn ngữ EN)
        String catNameEn = categoryDAO.getCategoryNameByLanguage(p.getCategory().getProductCategoryId(), "en")
                .orElse("Category " + p.getCategory().getProductCategoryId());
        categoryCombo.setSelectedItem(catNameEn);

        // Xóa nội dung cũ trong các trường dịch thuật
        translationFieldsMap.values().forEach(field -> field.setText(""));

        // Tải các bản dịch vào các trường nhập liệu động
        for (String langCode : translationFieldsMap.keySet()) {
            productDAO.getTranslationByIdAndLang(productId, langCode).ifPresent(pt -> {
                translationFieldsMap.get(langCode).setText(pt.getProductName());
            });
        }
    }

    // ====== LOAD DỮ LIỆU (Giữ nguyên) ======
    private void loadCategoriesIntoCombo() {
        categoryCombo.removeAllItems();
        categoryMap = new HashMap<>();
        try {
            // Lấy mã ID của ngôn ngữ EN (vì DAO cần ID hoặc code)
            Language enLang = languageDAO.getByCode("en");
            if (enLang == null) {
                categoryCombo.addItem("❌ Thiếu Ngôn ngữ EN");
                return;
            }

            List<ProductCategory> categories = categoryDAO.getAllCategories();
            for (ProductCategory cat : categories) {
                // Lấy tên danh mục bằng Tiếng Anh
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
        int selectedRow = productTable.getSelectedRow();

        // Lấy tên ngôn ngữ hiển thị
        String langName = langNameToCodeMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(currentLangCode))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("Product");

        tableModel.setColumnIdentifiers(new String[]{"ID", "Tên (" + langName + ")", "Giá", "Danh mục"});

        tableModel.setRowCount(0);
        try {
            List<ProductDisplayDTO> products = productService.getProductsForDisplay(currentLangCode);

            for (ProductDisplayDTO dto : products) {
                tableModel.addRow(new Object[]{
                        dto.getProductId(),
                        dto.getProductName(),
                        dto.getPrice(),
                        dto.getCategoryName()
                });
            }

            // Thiết lập lại Renderer sau khi thay đổi cột
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

            DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer();
            priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            productTable.getColumnModel().getColumn(2).setCellRenderer(priceRenderer);

            productTable.getColumnModel().getColumn(0).setMaxWidth(80);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            isLoading = false;
            if (selectedRow != -1 && selectedRow < productTable.getRowCount()) {
                productTable.setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }

    // ====== LISTENER CRUD ======

    private class AddProductListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String enName = translationFieldsMap.getOrDefault("en", new JTextField()).getText().trim();
            String priceStr = priceField.getText().trim();

            if (enName.isEmpty() || priceStr.isEmpty()) {
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

                ProductCategory cat = categoryDAO.getById(categoryMap.get(catName))
                        .orElseThrow(() -> new IllegalStateException("Category not found"));

                // 1. Tạo và Lưu Product gốc
                Product p = new Product(price, BigDecimal.ZERO, cat);
                productDAO.saveProduct(p);
                Integer newProductId = p.getProductId();

                // 2. Lưu tất cả các bản dịch từ form
                for (Map.Entry<String, JTextField> entry : translationFieldsMap.entrySet()) {
                    String langCode = entry.getKey();
                    String name = entry.getValue().getText().trim();

                    if (!name.isEmpty()) {
                        String descPlaceholder = "Mô tả tự động cho " + langCode.toUpperCase() + " (Thêm mới)";
                        productService.saveOrUpdateTranslation(newProductId, langCode, name, descPlaceholder);
                    }
                }

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

            // Kiểm tra tên EN (BẮT BUỘC)
            String enName = translationFieldsMap.getOrDefault("en", new JTextField()).getText().trim();
            if (enName.isEmpty()) {
                JOptionPane.showMessageDialog(ProductUI.this, "Tên sản phẩm Tiếng Anh không được để trống!", "Lỗi Dữ liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Chỉ cập nhật bản dịch (Giá và Danh mục bị vô hiệu hóa trong form)
                for (Map.Entry<String, JTextField> entry : translationFieldsMap.entrySet()) {
                    String langCode = entry.getKey();
                    String name = entry.getValue().getText().trim();

                    // Cập nhật hoặc thêm mới bản dịch nếu có nội dung
                    if (!name.isEmpty()) {
                        String descPlaceholder = "Mô tả cập nhật cho " + langCode.toUpperCase();
                        productService.saveOrUpdateTranslation(id, langCode, name, descPlaceholder);
                    }
                    // Nếu trường trống (Và không phải EN), ta bỏ qua, giữ lại bản dịch cũ nếu có.
                }

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
            int confirmation = JOptionPane.showConfirmDialog(ProductUI.this, "Bạn có chắc chắn muốn xóa sản phẩm có ID " + id + "? (Xóa tất cả bản dịch liên quan)", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    productService.deleteProduct(id);
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
        priceField.setText("");
        priceField.setEnabled(true);
        categoryCombo.setEnabled(true);
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }

        translationFieldsMap.values().forEach(field -> field.setText(""));

        productTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductUI().setVisible(true));
    }
}