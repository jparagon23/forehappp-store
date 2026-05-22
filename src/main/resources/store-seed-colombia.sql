-- Colombian Location Data Seed
-- Run ONCE after the schema migration has executed and the countries/states/cities tables exist.
-- Safe to re-run for countries (IGNORE on unique code), but states/cities will duplicate if run twice.

SET NAMES utf8mb4;

-- =====================
-- Country
-- =====================
INSERT IGNORE INTO countries (name, code, active) VALUES ('Colombia', 'COL', 1);
SET @co = (SELECT id FROM countries WHERE code = 'COL');

-- =====================
-- Departments (states)
-- =====================
INSERT INTO states (name, country_id, active) VALUES
  ('Amazonas',                 @co, 1),
  ('Antioquia',                @co, 1),
  ('Arauca',                   @co, 1),
  ('Atlántico',                @co, 1),
  ('Bogotá D.C.',              @co, 1),
  ('Bolívar',                  @co, 1),
  ('Boyacá',                   @co, 1),
  ('Caldas',                   @co, 1),
  ('Caquetá',                  @co, 1),
  ('Casanare',                 @co, 1),
  ('Cauca',                    @co, 1),
  ('Cesar',                    @co, 1),
  ('Chocó',                    @co, 1),
  ('Córdoba',                  @co, 1),
  ('Cundinamarca',             @co, 1),
  ('Guainía',                  @co, 1),
  ('Guaviare',                 @co, 1),
  ('Huila',                    @co, 1),
  ('La Guajira',               @co, 1),
  ('Magdalena',                @co, 1),
  ('Meta',                     @co, 1),
  ('Nariño',                   @co, 1),
  ('Norte de Santander',       @co, 1),
  ('Putumayo',                 @co, 1),
  ('Quindío',                  @co, 1),
  ('Risaralda',                @co, 1),
  ('San Andrés y Providencia', @co, 1),
  ('Santander',                @co, 1),
  ('Sucre',                    @co, 1),
  ('Tolima',                   @co, 1),
  ('Valle del Cauca',          @co, 1),
  ('Vaupés',                   @co, 1),
  ('Vichada',                  @co, 1);

-- =====================
-- Cities per department
-- =====================

SET @d = (SELECT id FROM states WHERE name = 'Amazonas' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Leticia',        @d, 1),
  ('Puerto Nariño',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Antioquia' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Medellín',    @d, 1),
  ('Bello',       @d, 1),
  ('Itagüí',      @d, 1),
  ('Envigado',    @d, 1),
  ('Apartadó',    @d, 1),
  ('Turbo',       @d, 1),
  ('Rionegro',    @d, 1),
  ('Sabaneta',    @d, 1),
  ('La Estrella', @d, 1),
  ('Copacabana',  @d, 1),
  ('Girardota',   @d, 1),
  ('Caldas',      @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Arauca' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Arauca',   @d, 1),
  ('Saravena', @d, 1),
  ('Tame',     @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Atlántico' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Barranquilla', @d, 1),
  ('Soledad',      @d, 1),
  ('Malambo',      @d, 1),
  ('Sabanalarga',  @d, 1),
  ('Baranoa',      @d, 1),
  ('Santo Tomás',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Bogotá D.C.' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Bogotá', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Bolívar' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Cartagena',            @d, 1),
  ('Magangué',             @d, 1),
  ('El Carmen de Bolívar', @d, 1),
  ('Mompox',               @d, 1),
  ('Turbaco',              @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Boyacá' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Tunja',        @d, 1),
  ('Duitama',      @d, 1),
  ('Sogamoso',     @d, 1),
  ('Chiquinquirá', @d, 1),
  ('Paipa',        @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Caldas' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Manizales',  @d, 1),
  ('Villamaría', @d, 1),
  ('La Dorada',  @d, 1),
  ('Riosucio',   @d, 1),
  ('Chinchiná',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Caquetá' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Florencia',              @d, 1),
  ('San Vicente del Caguán', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Casanare' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Yopal',      @d, 1),
  ('Aguazul',    @d, 1),
  ('Villanueva', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Cauca' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Popayán',                 @d, 1),
  ('Santander de Quilichao',  @d, 1),
  ('Puerto Tejada',           @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Cesar' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Valledupar', @d, 1),
  ('Aguachica',  @d, 1),
  ('Bosconia',   @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Chocó' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Quibdó',  @d, 1),
  ('Istmina', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Córdoba' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Montería',     @d, 1),
  ('Lorica',       @d, 1),
  ('Sahagún',      @d, 1),
  ('Planeta Rica', @d, 1),
  ('Cereté',       @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Cundinamarca' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Soacha',     @d, 1),
  ('Facatativá', @d, 1),
  ('Zipaquirá',  @d, 1),
  ('Chía',       @d, 1),
  ('Fusagasugá', @d, 1),
  ('Mosquera',   @d, 1),
  ('Madrid',     @d, 1),
  ('Cajicá',     @d, 1),
  ('Girardot',   @d, 1),
  ('Funza',      @d, 1),
  ('La Calera',  @d, 1),
  ('Sibaté',     @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Guainía' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Inírida', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Guaviare' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('San José del Guaviare', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Huila' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Neiva',    @d, 1),
  ('Pitalito', @d, 1),
  ('Garzón',   @d, 1),
  ('La Plata', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'La Guajira' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Riohacha', @d, 1),
  ('Maicao',   @d, 1),
  ('Uribia',   @d, 1),
  ('Manaure',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Magdalena' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Santa Marta', @d, 1),
  ('Ciénaga',     @d, 1),
  ('Fundación',   @d, 1),
  ('El Banco',    @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Meta' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Villavicencio', @d, 1),
  ('Acacías',       @d, 1),
  ('Granada',       @d, 1),
  ('Puerto López',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Nariño' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Pasto',     @d, 1),
  ('Tumaco',    @d, 1),
  ('Ipiales',   @d, 1),
  ('Túquerres', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Norte de Santander' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Cúcuta',            @d, 1),
  ('Ocaña',             @d, 1),
  ('Pamplona',          @d, 1),
  ('Villa del Rosario', @d, 1),
  ('Los Patios',        @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Putumayo' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Mocoa',       @d, 1),
  ('Puerto Asís', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Quindío' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Armenia',   @d, 1),
  ('Calarcá',   @d, 1),
  ('Montenegro', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Risaralda' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Pereira',             @d, 1),
  ('Dosquebradas',        @d, 1),
  ('Santa Rosa de Cabal', @d, 1),
  ('La Virginia',         @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'San Andrés y Providencia' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('San Andrés',  @d, 1),
  ('Providencia', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Santander' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Bucaramanga',     @d, 1),
  ('Floridablanca',   @d, 1),
  ('Girón',           @d, 1),
  ('Piedecuesta',     @d, 1),
  ('Barrancabermeja', @d, 1),
  ('Socorro',         @d, 1),
  ('San Gil',         @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Sucre' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Sincelejo',  @d, 1),
  ('Corozal',    @d, 1),
  ('San Marcos', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Tolima' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Ibagué',  @d, 1),
  ('Espinal', @d, 1),
  ('Melgar',  @d, 1),
  ('Honda',   @d, 1),
  ('Líbano',  @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Valle del Cauca' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Cali',         @d, 1),
  ('Buenaventura', @d, 1),
  ('Palmira',      @d, 1),
  ('Buga',         @d, 1),
  ('Tuluá',        @d, 1),
  ('Cartago',      @d, 1),
  ('Yumbo',        @d, 1),
  ('Jamundí',      @d, 1),
  ('Candelaria',   @d, 1),
  ('Florida',      @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Vaupés' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Mitú', @d, 1);

SET @d = (SELECT id FROM states WHERE name = 'Vichada' AND country_id = @co);
INSERT INTO cities (name, state_id, active) VALUES
  ('Puerto Carreño', @d, 1);
