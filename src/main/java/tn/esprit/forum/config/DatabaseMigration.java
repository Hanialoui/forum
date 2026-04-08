package tn.esprit.forum.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateColumns() {
        // Ensure all TEXT columns are actually TEXT (not varchar(255))
        String[] textColumns = {"sender_avatar", "content", "image_url", "gif_url",
                "reply_to_content", "reactions", "deleted_for_users"};
        for (String col : textColumns) {
            try {
                jdbcTemplate.execute("ALTER TABLE chat_message ALTER COLUMN " + col + " TYPE TEXT");
                System.out.println("[DB Migration] " + col + " column set to TEXT");
            } catch (Exception e) {
                System.out.println("[DB Migration] " + col + " already TEXT or missing: " + e.getMessage());
            }
        }

        try {
            jdbcTemplate.execute("ALTER TABLE chat_message ADD COLUMN IF NOT EXISTS is_forwarded BOOLEAN DEFAULT FALSE");
            System.out.println("[DB Migration] is_forwarded column ensured");
        } catch (Exception e) {
            System.out.println("[DB Migration] is_forwarded check: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE chat_message ADD COLUMN IF NOT EXISTS forwarded_from_name VARCHAR(255)");
            System.out.println("[DB Migration] forwarded_from_name column ensured");
        } catch (Exception e) {
            System.out.println("[DB Migration] forwarded_from_name check: " + e.getMessage());
        }
    }
}
