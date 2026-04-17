-- Trigger helpers

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_enforce_first_round_image()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.image_name IS NULL AND NEW.image_preview IS NULL THEN
        RETURN NEW;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM archive_tasks t
        WHERE t.archive_id = NEW.archive_id
          AND t.id <> COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
    ) THEN
        RAISE EXCEPTION 'Only the first task in an archive can include an image';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_sync_archive_visibility_with_publication()
RETURNS TRIGGER AS $$
BEGIN
    -- Publication owner must match archive owner.
    IF NOT EXISTS (
        SELECT 1
        FROM archives a
        WHERE a.id = NEW.archive_id
          AND a.owner_id = NEW.owner_id
    ) THEN
        RAISE EXCEPTION 'Publication owner must equal archive owner';
    END IF;

    -- Active publication means archive should be public; inactive means private.
    UPDATE archives
    SET is_private = NOT NEW.is_active,
        updated_at = NOW()
    WHERE id = NEW.archive_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_update_remix_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE community_publications
        SET remix_count = remix_count + 1,
            updated_at = NOW()
        WHERE id = NEW.publication_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE community_publications
        SET remix_count = GREATEST(remix_count - 1, 0),
            updated_at = NOW()
        WHERE id = OLD.publication_id;
        RETURN OLD;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_archives_set_updated_at ON archives;
CREATE TRIGGER trg_archives_set_updated_at
BEFORE UPDATE ON archives
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_archive_tasks_set_updated_at ON archive_tasks;
CREATE TRIGGER trg_archive_tasks_set_updated_at
BEFORE UPDATE ON archive_tasks
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_archive_model_assets_set_updated_at ON archive_model_assets;
CREATE TRIGGER trg_archive_model_assets_set_updated_at
BEFORE UPDATE ON archive_model_assets
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_community_publications_set_updated_at ON community_publications;
CREATE TRIGGER trg_community_publications_set_updated_at
BEFORE UPDATE ON community_publications
FOR EACH ROW
EXECUTE FUNCTION fn_set_updated_at();

DROP TRIGGER IF EXISTS trg_archive_tasks_first_round_image ON archive_tasks;
CREATE TRIGGER trg_archive_tasks_first_round_image
BEFORE INSERT OR UPDATE OF image_name, image_preview ON archive_tasks
FOR EACH ROW
EXECUTE FUNCTION fn_enforce_first_round_image();

DROP TRIGGER IF EXISTS trg_publication_sync_archive_visibility ON community_publications;
CREATE TRIGGER trg_publication_sync_archive_visibility
AFTER INSERT OR UPDATE OF is_active, owner_id ON community_publications
FOR EACH ROW
EXECUTE FUNCTION fn_sync_archive_visibility_with_publication();

DROP TRIGGER IF EXISTS trg_remix_events_update_count_insert ON remix_events;
CREATE TRIGGER trg_remix_events_update_count_insert
AFTER INSERT ON remix_events
FOR EACH ROW
EXECUTE FUNCTION fn_update_remix_count();

DROP TRIGGER IF EXISTS trg_remix_events_update_count_delete ON remix_events;
CREATE TRIGGER trg_remix_events_update_count_delete
AFTER DELETE ON remix_events
FOR EACH ROW
EXECUTE FUNCTION fn_update_remix_count();

