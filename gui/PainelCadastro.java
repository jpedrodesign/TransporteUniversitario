package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class PainelCadastro extends JPanel {

    private JTextField txtNome;
    private JTextField txtLatitude;
    private JTextField txtLongitude;
    private JTextField txtAlunos;

    private JButton btnCadastrar;

    public PainelCadastro() {

        setLayout(new GridBagLayout());

        setPreferredSize(
                new Dimension(250, 600)
        );

        setBorder(
                BorderFactory.createTitledBorder(
                        "Cadastro"
                )
        );

        txtNome = new JTextField();
        txtNome.setPreferredSize(new Dimension(220, 32));

        txtLatitude = new JTextField();
        txtLatitude.setText("-12.6700");
        txtLatitude.setPreferredSize(new Dimension(220, 32));

        txtLongitude = new JTextField();
        txtLongitude.setText("-39.1019");
        txtLongitude.setPreferredSize(new Dimension(220, 32));

        txtAlunos = new JTextField();
        txtAlunos.setText("20");
        txtAlunos.setPreferredSize(new Dimension(220, 32));

        btnCadastrar =
                new JButton("Adicionar ponto") {

                    @Override
                    protected void paintComponent(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );
                        g2.setColor(getModel().isRollover()
                                ? new Color(45, 116, 184)
                                : new Color(34, 94, 154));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.dispose();

                        super.paintComponent(g);
                    }
                };
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setContentAreaFilled(false);
        btnCadastrar.setBorderPainted(false);
        btnCadastrar.setForeground(Color.WHITE);
        btnCadastrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCadastrar.setUI(new BasicButtonUI());
        btnCadastrar.setPreferredSize(new Dimension(220, 38));
        btnCadastrar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(28, 70, 117)),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                )
        );

        JTextArea ajuda =
                new JTextArea(
                        "Use latitude/longitude de Cruz das Almas. " +
                                "O sistema liga o novo ponto aos vizinhos mais proximos."
                );

        ajuda.setLineWrap(true);
        ajuda.setWrapStyleWord(true);
        ajuda.setEditable(false);
        ajuda.setOpaque(false);
        ajuda.setPreferredSize(new Dimension(220, 80));
        ajuda.setMinimumSize(new Dimension(220, 80));
        ajuda.setMaximumSize(new Dimension(220, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;

        adicionarLinha(this, gbc, new JLabel("Nome do ponto"), txtNome);
        adicionarLinha(this, gbc, new JLabel("Latitude"), txtLatitude);
        adicionarLinha(this, gbc, new JLabel("Longitude"), txtLongitude);
        adicionarLinha(this, gbc, new JLabel("Qtd. Alunos"), txtAlunos);

        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 4, 0);
        add(btnCadastrar, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.weighty = 1.0;
        add(ajuda, gbc);
    }

    private void adicionarLinha(
            Container container,
            GridBagConstraints gbc,
            JLabel label,
            JTextField campo
    ) {
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(2, 0, 2, 0);
        container.add(label, gbc);

        gbc.gridy++;
        gbc.weightx = 1.0;
        container.add(campo, gbc);
    }

    public JButton getBtnCadastrar() {
        return btnCadastrar;
    }

    public String getNome() {
        return txtNome.getText();
    }

    public double getLatitude() {

        return Double.parseDouble(
                txtLatitude.getText()
        );
    }

    public double getLongitude() {

        return Double.parseDouble(
                txtLongitude.getText()
        );
    }

    public int getQuantidadeAlunos() {

        return Integer.parseInt(
                txtAlunos.getText()
        );
    }

    public void limparCampos() {

        txtNome.setText("");

        txtLatitude.setText("");

        txtLongitude.setText("");

        txtAlunos.setText("");
    }
}
