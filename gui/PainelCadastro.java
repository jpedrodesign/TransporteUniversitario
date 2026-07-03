package gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class PainelCadastro extends JPanel {

    private JTextField txtNome;
    private JTextField txtLatitude;
    private JTextField txtLongitude;
    private JTextField txtAlunos;

    private JButton btnCadastrar;

    public PainelCadastro() {

        setLayout(new GridLayout(11, 1, 5, 5));

        setPreferredSize(
                new Dimension(250, 600)
        );

        setBorder(
                BorderFactory.createTitledBorder(
                        "Cadastro"
                )
        );

        txtNome = new JTextField();

        txtLatitude = new JTextField();
        txtLatitude.setText("-12.6700");

        txtLongitude = new JTextField();
        txtLongitude.setText("-39.1019");

        txtAlunos = new JTextField();
        txtAlunos.setText("20");

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

        add(new JLabel("Nome do ponto"));
        add(txtNome);

        add(new JLabel("Latitude"));
        add(txtLatitude);

        add(new JLabel("Longitude"));
        add(txtLongitude);

        add(new JLabel("Qtd. Alunos"));
        add(txtAlunos);

        add(btnCadastrar);
        add(ajuda);
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
