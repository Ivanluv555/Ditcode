-- Business constraints and FK added after base tables exist

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_archives_status') THEN
        ALTER TABLE archives
            ADD CONSTRAINT chk_archives_status
            CHECK (status IN ('idle', 'inferencing', 'success', 'failed'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_archives_source_not_self') THEN
        ALTER TABLE archives
            ADD CONSTRAINT chk_archives_source_not_self
            CHECK (source_archive_id IS NULL OR source_archive_id <> id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_archive_messages_role') THEN
        ALTER TABLE archive_messages
            ADD CONSTRAINT chk_archive_messages_role
            CHECK (role IN ('user', 'assistant', 'system'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_archive_tasks_status') THEN
        ALTER TABLE archive_tasks
            ADD CONSTRAINT chk_archive_tasks_status
            CHECK (status IN ('queued', 'inferencing', 'success', 'failed'));
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_archive_tasks_progress') THEN
        ALTER TABLE archive_tasks
            ADD CONSTRAINT chk_archive_tasks_progress
            CHECK (progress >= 0 AND progress <= 100);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_community_publications_remix_count') THEN
        ALTER TABLE community_publications
            ADD CONSTRAINT chk_community_publications_remix_count
            CHECK (remix_count >= 0);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_archives_source_community') THEN
        ALTER TABLE archives
            ADD CONSTRAINT fk_archives_source_community
            FOREIGN KEY (source_community_id) REFERENCES community_publications(id);
    END IF;
END
$$;
