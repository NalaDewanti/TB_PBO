import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

// Interface PengelolaBuku
interface PengelolaBuku {
    void tambahBuku(Buku buku);
    void perbaruiBuku(Buku buku);
    void hapusBuku(int idBuku);
    List<Buku> ambilSemuaBuku();
    Buku ambilBukuBerdasarkanId(int idBuku);
}

// Enum JenisBuku
enum JenisBuku {
    FIKSI,
    PELAJARAN
}

// Class Buku
class Buku {
    private int id;
    private String judul;
    private String penulis;
    private int stok;
    private JenisBuku jenisBuku;

    public Buku(int id, String judul, String penulis, int stok, JenisBuku jenisBuku) {
        this.id = id;
        this.judul = judul;
        this.penulis = penulis;
        this.stok = stok;
        this.jenisBuku = jenisBuku;
    }

    public int getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public String getPenulis() {
        return penulis;
    }

    public int getStok() {
        return stok;
    }

    public JenisBuku getJenisBuku() {
        return jenisBuku;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Judul: " + judul + ", Penulis: " + penulis + ", Stok: " + stok + ", Jenis: " + jenisBuku;
    }
}

// Implementasi PengelolaBuku dengan JDBC
class PengelolaBukuImpl implements PengelolaBuku {
    private Connection koneksi;

    // Constructor yang mencoba untuk membuat koneksi ke database
    public PengelolaBukuImpl() {
        try {
            // Menghubungkan ke database, pastikan kredensial dan URL sudah benar
            this.koneksi = DriverManager.getConnection("jdbc:mysql://localhost:3306/library app", "root", "");
            if (koneksi != null) {
                System.out.println("Koneksi ke database berhasil!");
            }
        } catch (SQLException e) {
            System.out.println("Kesalahan saat membuat koneksi: " + e.getMessage());
        }
    }

    @Override
    public void tambahBuku(Buku buku) {
        // Pastikan koneksi valid sebelum menjalankan query
        if (koneksi == null) {
            System.out.println("Koneksi ke database tidak tersedia.");
            return;
        }

        String query = "INSERT INTO buku (id, judul, penulis, stok, jenis) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = koneksi.prepareStatement(query)) {
            statement.setInt(1, buku.getId()); // Menyertakan ID yang dimasukkan secara manual
            statement.setString(2, buku.getJudul());
            statement.setString(3, buku.getPenulis());
            statement.setInt(4, buku.getStok());
            statement.setString(5, buku.getJenisBuku().name()); // Menyimpan jenis buku sebagai string
            statement.executeUpdate();
            System.out.println("Buku berhasil ditambahkan.");
        } catch (SQLException e) {
            System.out.println("Kesalahan saat menambahkan buku: " + e.getMessage());
        }
    }

    @Override
    public void perbaruiBuku(Buku buku) {
        String query = "UPDATE buku SET judul = ?, penulis = ?, stok = ?, jenis = ? WHERE id = ?";
        try (PreparedStatement statement = koneksi.prepareStatement(query)) {
            statement.setString(1, buku.getJudul());
            statement.setString(2, buku.getPenulis());
            statement.setInt(3, buku.getStok());
            statement.setString(4, buku.getJenisBuku().name());
            statement.setInt(5, buku.getId());
            statement.executeUpdate();
            System.out.println("Buku berhasil diperbarui.");
        } catch (SQLException e) {
            System.out.println("Kesalahan saat memperbarui buku: " + e.getMessage());
        }
    }

    @Override
    public void hapusBuku(int idBuku) {
        String query = "DELETE FROM buku WHERE id = ?";
        try (PreparedStatement statement = koneksi.prepareStatement(query)) {
            statement.setInt(1, idBuku);
            statement.executeUpdate();
            System.out.println("Buku berhasil dihapus.");
        } catch (SQLException e) {
            System.out.println("Kesalahan saat menghapus buku: " + e.getMessage());
        }
    }

    @Override
    public List<Buku> ambilSemuaBuku() {
        List<Buku> bukuList = new ArrayList<>();
        String query = "SELECT * FROM buku";
        try (Statement statement = koneksi.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Buku buku = new Buku(
                    resultSet.getInt("id"),
                    resultSet.getString("judul"),
                    resultSet.getString("penulis"),
                    resultSet.getInt("stok"),
                    JenisBuku.valueOf(resultSet.getString("jenis"))
                );
                bukuList.add(buku);
            }
        } catch (SQLException e) {
            System.out.println("Kesalahan saat mengambil data buku: " + e.getMessage());
        }
        return bukuList;
    }

    @Override
    public Buku ambilBukuBerdasarkanId(int idBuku) {
        String query = "SELECT * FROM buku WHERE id = ?";
        try (PreparedStatement statement = koneksi.prepareStatement(query)) {
            statement.setInt(1, idBuku);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Buku(
                        resultSet.getInt("id"),
                        resultSet.getString("judul"),
                        resultSet.getString("penulis"),
                        resultSet.getInt("stok"),
                        JenisBuku.valueOf(resultSet.getString("jenis"))
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Kesalahan saat mengambil buku berdasarkan ID: " + e.getMessage());
        }
        return null;
    }
}
// Aplikasi Utama untuk interaksi dengan pengguna
public class LibraryApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        PengelolaBuku pengelolaBuku = new PengelolaBukuImpl();
        boolean programBerjalan = true;

        while (programBerjalan) {
            System.out.println("\nSistem Manajemen Perpustakaan");
            System.out.println("1. Tambah Buku");
            System.out.println("2. Perbarui Buku");
            System.out.println("3. Hapus Buku");
            System.out.println("4. Lihat Semua Buku");
            System.out.println("5. Tambah Stok Buku");
            System.out.println("6. Keluar");
            System.out.print("Masukkan pilihan: ");

            int pilihan = 0;
            try {
                pilihan = scanner.nextInt();
                scanner.nextLine(); // Konsumsi newline
            } catch (InputMismatchException e) {
                scanner.nextLine(); // Konsumsi input yang tidak valid
                System.out.println("Pilihan tidak valid. Harap masukkan angka antara 1 dan 5.");
                continue;
            }

            switch (pilihan) {
                case 1:
                    // Pilihan jenis buku (Fiksi atau Pelajaran)
                    System.out.println("Pilih jenis buku:");
                    System.out.println("1. Buku Fiksi");
                    System.out.println("2. Buku Pelajaran");
                    System.out.print("Masukkan pilihan: ");
                    int jenisBukuChoice = scanner.nextInt();
                    scanner.nextLine(); // Konsumsi newline

                    JenisBuku jenisBuku;
                    if (jenisBukuChoice == 1) {
                        jenisBuku = JenisBuku.FIKSI;
                    } else if (jenisBukuChoice == 2) {
                        jenisBuku = JenisBuku.PELAJARAN;
                    } else {
                        System.out.println("Jenis buku tidak valid, memilih Buku Fiksi secara default.");
                        jenisBuku = JenisBuku.FIKSI;
                    }

                    // Menambahkan ID Buku secara manual setelah memilih jenis buku
                    System.out.print("Masukkan ID buku: ");
                    int idBuku = scanner.nextInt();
                    scanner.nextLine(); // Konsumsi newline
                    System.out.print("Masukkan judul buku: ");
                    String judul = scanner.nextLine();
                    System.out.print("Masukkan penulis buku: ");
                    String penulis = scanner.nextLine();
                    System.out.print("Masukkan stok buku: ");
                    int stok = scanner.nextInt();

                    pengelolaBuku.tambahBuku(new Buku(idBuku, judul, penulis, stok, jenisBuku));
                    break;

                case 2:
                    System.out.print("Masukkan ID buku yang ingin diperbarui: ");
                    int idUpdate = scanner.nextInt();
                    scanner.nextLine(); // Konsumsi newline
                    System.out.print("Masukkan judul baru: ");
                    String judulBaru = scanner.nextLine();
                    System.out.print("Masukkan penulis baru: ");
                    String penulisBaru = scanner.nextLine();
                    System.out.print("Masukkan stok baru: ");
                    int stokBaru = scanner.nextInt();

                    // Pilihan jenis buku (Fiksi atau Pelajaran)
                    System.out.println("Pilih jenis buku:");
                    System.out.println("1. Buku Fiksi");
                    System.out.println("2. Buku Pelajaran");
                    System.out.print("Masukkan pilihan: ");
                    int jenisBukuUpdate = scanner.nextInt();
                    scanner.nextLine(); // Konsumsi newline

                    JenisBuku jenisBukuBaru;
                    if (jenisBukuUpdate == 1) {
                        jenisBukuBaru = JenisBuku.FIKSI;
                    } else {
                        jenisBukuBaru = JenisBuku.PELAJARAN;
                    }

                    pengelolaBuku.perbaruiBuku(new Buku(idUpdate, judulBaru, penulisBaru, stokBaru, jenisBukuBaru));
                    break;

                case 3:
                    System.out.print("Masukkan ID buku yang ingin dihapus: ");
                    int idHapus = scanner.nextInt();
                    pengelolaBuku.hapusBuku(idHapus);
                    break;

                case 4:
                    List<Buku> bukuList = pengelolaBuku.ambilSemuaBuku();
                    if (bukuList.isEmpty()) {
                        System.out.println("Tidak ada buku tersedia.");
                    } else {
                        for (Buku buku : bukuList) {
                            System.out.println(buku);
                        }
                    }
                    break;
                    case 5:
    System.out.print("Masukkan ID buku yang ingin ditambahkan stoknya: ");
    int idTambahStok = scanner.nextInt();
    scanner.nextLine(); // Konsumsi newline

    Buku bukuYangDitemukan = pengelolaBuku.ambilBukuBerdasarkanId(idTambahStok);
    if (bukuYangDitemukan == null) {
        System.out.println("Buku dengan ID " + idTambahStok + " tidak ditemukan.");
    } else {
        System.out.println("Buku ditemukan: " + bukuYangDitemukan);
        System.out.print("Masukkan jumlah stok yang ingin ditambahkan: ");
        int jumlahTambahStok = scanner.nextInt();

        if (jumlahTambahStok > 0) {
            int stokDiperbarui = bukuYangDitemukan.getStok() + jumlahTambahStok;
            bukuYangDitemukan.setStok(stokDiperbarui);
            pengelolaBuku.perbaruiBuku(bukuYangDitemukan); // Update stok di database
            System.out.println("Stok berhasil ditambahkan. Stok baru: " + stokDiperbarui);
        } else {
            System.out.println("Jumlah stok yang ditambahkan harus lebih dari 0.");
        }
    }
    break;

                case 6:
                    programBerjalan = false;
                    break;

                default:
                    System.out.println("Pilihan tidak valid. Harap coba lagi.");
            }
        }

        scanner.close();
    }
}
