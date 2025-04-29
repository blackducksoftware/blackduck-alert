DO $$
BEGIN -- Start the main procedural block
    BEGIN -- Attempt to create the function
        CREATE FUNCTION GET_CONTEXT_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            SELECT ID
            INTO result
            FROM ALERT.CONFIG_CONTEXTS
            WHERE CONTEXT = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        -- Print success message if function is created successfully
        RAISE INFO 'Function GET_CONTEXT_ID created successfully.';
    EXCEPTION -- Handle the case where the function already exists
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_CONTEXT_ID already exists.';
    END; -- End of the nested block
END; -- End of the main procedural block
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_FIELD_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result from ALERT.DEFINED_FIELDS
            where SOURCE_KEY = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_FIELD_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_FIELD_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_DESCRIPTOR_TYPE_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.DESCRIPTOR_TYPES where TYPE = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_DESCRIPTOR_TYPE_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_DESCRIPTOR_TYPE_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_DESCRIPTOR_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.REGISTERED_DESCRIPTORS where NAME = $1;
        RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_DESCRIPTOR_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_DESCRIPTOR_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_ROLE_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.ROLES where ROLENAME = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_ROLE_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_ROLE_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_AUTH_TYPE_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.AUTHENTICATION_TYPE where NAME = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_AUTH_TYPE_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_AUTH_TYPE_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_USER_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.USERS where USERNAME = $1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_USER_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_USER_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_PROVIDER_CONFIG_ID(text)
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.DESCRIPTOR_CONFIGS
            where DESCRIPTOR_ID = GET_DESCRIPTOR_ID($1)
            and CONTEXT_ID = GET_CONTEXT_ID('GLOBAL')
            LIMIT 1;
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_PROVIDER_CONFIG_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_PROVIDER_CONFIG_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_OLD_BLACK_DUCK_PROVIDER_CONFIG_ID()
        RETURNS BIGINT AS
        $result$
        DECLARE result BIGINT;
        BEGIN
            select ID
            into result
            from ALERT.DESCRIPTOR_CONFIGS where CONTEXT_ID = GET_CONTEXT_ID('GLOBAL') and DESCRIPTOR_ID = GET_DESCRIPTOR_ID('provider_blackduck');
            RETURN result;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_OLD_BLACK_DUCK_PROVIDER_CONFIG_ID created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_OLD_BLACK_DUCK_PROVIDER_CONFIG_ID already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_JOB_ID_SOURCE_KEY_AND_FIELD_VALUE()
        RETURNS table (job_id UUID, source_key VARCHAR, field_value VARCHAR) AS
        $result$
        BEGIN
        RETURN QUERY
            SELECT job.job_id, field.source_key, fv.field_value
                FROM alert.config_groups job
                INNER JOIN alert.descriptor_configs config ON config.id = job.config_id
                LEFT JOIN alert.field_values fv ON fv.config_id = config.id
                INNER JOIN alert.defined_fields field on field.id = fv.field_id;
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_JOB_ID_SOURCE_KEY_AND_FIELD_VALUE created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_JOB_ID_SOURCE_KEY_AND_FIELD_VALUE already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_GLOBAL_CONFIG_SOURCE_KEY_AND_FIELD_VALUE(text)
        RETURNS table (source_key VARCHAR, field_value VARCHAR) AS
        $result$
        BEGIN
        RETURN QUERY
            SELECT field.source_key, fv.field_value
                FROM alert.descriptor_configs config
                LEFT JOIN alert.field_values fv ON fv.config_id = config.id
                INNER JOIN alert.defined_fields field on field.id = fv.field_id
                WHERE config.descriptor_id = GET_DESCRIPTOR_ID($1)
                and config.context_id = GET_CONTEXT_ID('GLOBAL');
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_GLOBAL_CONFIG_SOURCE_KEY_AND_FIELD_VALUE created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_GLOBAL_CONFIG_SOURCE_KEY_AND_FIELD_VALUE already exists.';
    END;
END;
$$;

DO $$
BEGIN
    BEGIN
        CREATE FUNCTION GET_GLOBAL_CONFIG_TIMESTAMPS(text)
        RETURNS table (created_at TIMESTAMP WITH TIME ZONE, last_updated TIMESTAMP WITH TIME ZONE) AS
        $result$
        BEGIN
        RETURN QUERY
            SELECT config.created_at, config.last_updated
                FROM alert.descriptor_configs config
                WHERE config.descriptor_id = GET_DESCRIPTOR_ID($1)
                and config.context_id = GET_CONTEXT_ID('GLOBAL');
        END;
        $result$
        LANGUAGE plpgsql;

        RAISE INFO 'Function GET_GLOBAL_CONFIG_TIMESTAMPS created successfully.';
    EXCEPTION
        WHEN duplicate_function THEN
            RAISE INFO 'Function GET_GLOBAL_CONFIG_TIMESTAMPS already exists.';
    END;
END;
$$;