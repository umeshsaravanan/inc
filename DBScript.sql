PGDMP  !    0            
    |            compilation    16.4    16.4 :    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            �           1262    16441    compilation    DATABASE     ~   CREATE DATABASE compilation WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_India.1252';
    DROP DATABASE compilation;
                postgres    false            �            1259    16525    filedetails    TABLE     �   CREATE TABLE public.filedetails (
    file_id integer NOT NULL,
    programfile bytea,
    file_name character varying(50) NOT NULL,
    user_id integer NOT NULL,
    compiletime double precision,
    memory integer,
    access integer NOT NULL
);
    DROP TABLE public.filedetails;
       public         heap    postgres    false            �            1259    16524    filedetails_file_id_seq    SEQUENCE     �   CREATE SEQUENCE public.filedetails_file_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.filedetails_file_id_seq;
       public          postgres    false    218            �           0    0    filedetails_file_id_seq    SEQUENCE OWNED BY     S   ALTER SEQUENCE public.filedetails_file_id_seq OWNED BY public.filedetails.file_id;
          public          postgres    false    217            �            1259    24666 
   filestable    TABLE     �   CREATE TABLE public.filestable (
    file_id integer NOT NULL,
    file_name character varying(100) NOT NULL,
    size double precision NOT NULL,
    folder_id integer NOT NULL,
    user_id integer NOT NULL,
    date character varying(16) NOT NULL
);
    DROP TABLE public.filestable;
       public         heap    postgres    false            �            1259    24665    filestable_file_id_seq    SEQUENCE     �   CREATE SEQUENCE public.filestable_file_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.filestable_file_id_seq;
       public          postgres    false    222            �           0    0    filestable_file_id_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.filestable_file_id_seq OWNED BY public.filestable.file_id;
          public          postgres    false    221            �            1259    24677    foldermapper    TABLE     c   CREATE TABLE public.foldermapper (
    user_id integer NOT NULL,
    folder_id integer NOT NULL
);
     DROP TABLE public.foldermapper;
       public         heap    postgres    false            �            1259    24647    foldertable    TABLE     )  CREATE TABLE public.foldertable (
    folder_id integer NOT NULL,
    folder_name character varying(100) NOT NULL,
    user_id integer NOT NULL,
    parent_folder integer,
    path character varying(1000) NOT NULL,
    size double precision DEFAULT 0.0,
    date character varying(16) NOT NULL
);
    DROP TABLE public.foldertable;
       public         heap    postgres    false            �            1259    24646    foldertable_folder_id_seq    SEQUENCE     �   CREATE SEQUENCE public.foldertable_folder_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 0   DROP SEQUENCE public.foldertable_folder_id_seq;
       public          postgres    false    220            �           0    0    foldertable_folder_id_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE public.foldertable_folder_id_seq OWNED BY public.foldertable.folder_id;
          public          postgres    false    219            �            1259    32914    historyfiles    TABLE     	  CREATE TABLE public.historyfiles (
    file_name character varying NOT NULL,
    file_id integer NOT NULL,
    parent_id integer NOT NULL,
    size integer NOT NULL,
    size_diff integer NOT NULL,
    status character(15) NOT NULL,
    user_id integer NOT NULL
);
     DROP TABLE public.historyfiles;
       public         heap    postgres    false            �            1259    32904    historyfolders    TABLE     )  CREATE TABLE public.historyfolders (
    f_id integer NOT NULL,
    user_id integer NOT NULL,
    f_name character varying(50) NOT NULL,
    pf_id integer,
    path character varying NOT NULL,
    status character varying(15) NOT NULL,
    size integer NOT NULL,
    size_diff integer NOT NULL
);
 "   DROP TABLE public.historyfolders;
       public         heap    postgres    false            �            1259    32903    historyfolders_f_id_seq    SEQUENCE     �   CREATE SEQUENCE public.historyfolders_f_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.historyfolders_f_id_seq;
       public          postgres    false    226            �           0    0    historyfolders_f_id_seq    SEQUENCE OWNED BY     S   ALTER SEQUENCE public.historyfolders_f_id_seq OWNED BY public.historyfolders.f_id;
          public          postgres    false    225            �            1259    32900    historymapper    TABLE     �   CREATE TABLE public.historymapper (
    user_id integer NOT NULL,
    f_id integer NOT NULL,
    compared_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
 !   DROP TABLE public.historymapper;
       public         heap    postgres    false            �            1259    32913    hostoryfiles_file_id_seq    SEQUENCE     �   CREATE SEQUENCE public.hostoryfiles_file_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 /   DROP SEQUENCE public.hostoryfiles_file_id_seq;
       public          postgres    false    228                        0    0    hostoryfiles_file_id_seq    SEQUENCE OWNED BY     U   ALTER SEQUENCE public.hostoryfiles_file_id_seq OWNED BY public.historyfiles.file_id;
          public          postgres    false    227            �            1259    16464    userdetails    TABLE     �   CREATE TABLE public.userdetails (
    user_id integer NOT NULL,
    user_name character varying(30) NOT NULL,
    password character varying(100) NOT NULL
);
    DROP TABLE public.userdetails;
       public         heap    postgres    false            �            1259    16463    userdetails_user_id_seq    SEQUENCE     �   CREATE SEQUENCE public.userdetails_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.userdetails_user_id_seq;
       public          postgres    false    216                       0    0    userdetails_user_id_seq    SEQUENCE OWNED BY     S   ALTER SEQUENCE public.userdetails_user_id_seq OWNED BY public.userdetails.user_id;
          public          postgres    false    215            <           2604    16528    filedetails file_id    DEFAULT     z   ALTER TABLE ONLY public.filedetails ALTER COLUMN file_id SET DEFAULT nextval('public.filedetails_file_id_seq'::regclass);
 B   ALTER TABLE public.filedetails ALTER COLUMN file_id DROP DEFAULT;
       public          postgres    false    218    217    218            ?           2604    24669    filestable file_id    DEFAULT     x   ALTER TABLE ONLY public.filestable ALTER COLUMN file_id SET DEFAULT nextval('public.filestable_file_id_seq'::regclass);
 A   ALTER TABLE public.filestable ALTER COLUMN file_id DROP DEFAULT;
       public          postgres    false    221    222    222            =           2604    24650    foldertable folder_id    DEFAULT     ~   ALTER TABLE ONLY public.foldertable ALTER COLUMN folder_id SET DEFAULT nextval('public.foldertable_folder_id_seq'::regclass);
 D   ALTER TABLE public.foldertable ALTER COLUMN folder_id DROP DEFAULT;
       public          postgres    false    219    220    220            B           2604    32917    historyfiles file_id    DEFAULT     |   ALTER TABLE ONLY public.historyfiles ALTER COLUMN file_id SET DEFAULT nextval('public.hostoryfiles_file_id_seq'::regclass);
 C   ALTER TABLE public.historyfiles ALTER COLUMN file_id DROP DEFAULT;
       public          postgres    false    227    228    228            A           2604    32907    historyfolders f_id    DEFAULT     z   ALTER TABLE ONLY public.historyfolders ALTER COLUMN f_id SET DEFAULT nextval('public.historyfolders_f_id_seq'::regclass);
 B   ALTER TABLE public.historyfolders ALTER COLUMN f_id DROP DEFAULT;
       public          postgres    false    226    225    226            ;           2604    16467    userdetails user_id    DEFAULT     z   ALTER TABLE ONLY public.userdetails ALTER COLUMN user_id SET DEFAULT nextval('public.userdetails_user_id_seq'::regclass);
 B   ALTER TABLE public.userdetails ALTER COLUMN user_id DROP DEFAULT;
       public          postgres    false    216    215    216            �          0    16525    filedetails 
   TABLE DATA           l   COPY public.filedetails (file_id, programfile, file_name, user_id, compiletime, memory, access) FROM stdin;
    public          postgres    false    218   �E       �          0    24666 
   filestable 
   TABLE DATA           X   COPY public.filestable (file_id, file_name, size, folder_id, user_id, date) FROM stdin;
    public          postgres    false    222   �N       �          0    24677    foldermapper 
   TABLE DATA           :   COPY public.foldermapper (user_id, folder_id) FROM stdin;
    public          postgres    false    223   �d       �          0    24647    foldertable 
   TABLE DATA           g   COPY public.foldertable (folder_id, folder_name, user_id, parent_folder, path, size, date) FROM stdin;
    public          postgres    false    220   �d       �          0    32914    historyfiles 
   TABLE DATA           g   COPY public.historyfiles (file_name, file_id, parent_id, size, size_diff, status, user_id) FROM stdin;
    public          postgres    false    228   �h       �          0    32904    historyfolders 
   TABLE DATA           e   COPY public.historyfolders (f_id, user_id, f_name, pf_id, path, status, size, size_diff) FROM stdin;
    public          postgres    false    226   Tl       �          0    32900    historymapper 
   TABLE DATA           E   COPY public.historymapper (user_id, f_id, compared_date) FROM stdin;
    public          postgres    false    224   qm       �          0    16464    userdetails 
   TABLE DATA           C   COPY public.userdetails (user_id, user_name, password) FROM stdin;
    public          postgres    false    216   �m                  0    0    filedetails_file_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.filedetails_file_id_seq', 34, true);
          public          postgres    false    217                       0    0    filestable_file_id_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('public.filestable_file_id_seq', 37365, true);
          public          postgres    false    221                       0    0    foldertable_folder_id_seq    SEQUENCE SET     J   SELECT pg_catalog.setval('public.foldertable_folder_id_seq', 6697, true);
          public          postgres    false    219                       0    0    historyfolders_f_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.historyfolders_f_id_seq', 39, true);
          public          postgres    false    225                       0    0    hostoryfiles_file_id_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('public.hostoryfiles_file_id_seq', 163, true);
          public          postgres    false    227                       0    0    userdetails_user_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.userdetails_user_id_seq', 39, true);
          public          postgres    false    215            H           2606    16532    filedetails filedetails_pkey 
   CONSTRAINT     j   ALTER TABLE ONLY public.filedetails
    ADD CONSTRAINT filedetails_pkey PRIMARY KEY (user_id, file_name);
 F   ALTER TABLE ONLY public.filedetails DROP CONSTRAINT filedetails_pkey;
       public            postgres    false    218    218            L           2606    24671    filestable filestable_pkey 
   CONSTRAINT     ]   ALTER TABLE ONLY public.filestable
    ADD CONSTRAINT filestable_pkey PRIMARY KEY (file_id);
 D   ALTER TABLE ONLY public.filestable DROP CONSTRAINT filestable_pkey;
       public            postgres    false    222            N           2606    24681    foldermapper foldermapper_pkey 
   CONSTRAINT     l   ALTER TABLE ONLY public.foldermapper
    ADD CONSTRAINT foldermapper_pkey PRIMARY KEY (user_id, folder_id);
 H   ALTER TABLE ONLY public.foldermapper DROP CONSTRAINT foldermapper_pkey;
       public            postgres    false    223    223            J           2606    24652    foldertable foldertable_pkey 
   CONSTRAINT     a   ALTER TABLE ONLY public.foldertable
    ADD CONSTRAINT foldertable_pkey PRIMARY KEY (folder_id);
 F   ALTER TABLE ONLY public.foldertable DROP CONSTRAINT foldertable_pkey;
       public            postgres    false    220            P           2606    32911 "   historyfolders historyfolders_pkey 
   CONSTRAINT     b   ALTER TABLE ONLY public.historyfolders
    ADD CONSTRAINT historyfolders_pkey PRIMARY KEY (f_id);
 L   ALTER TABLE ONLY public.historyfolders DROP CONSTRAINT historyfolders_pkey;
       public            postgres    false    226            R           2606    32921    historyfiles hostoryfiles_pkey 
   CONSTRAINT     a   ALTER TABLE ONLY public.historyfiles
    ADD CONSTRAINT hostoryfiles_pkey PRIMARY KEY (file_id);
 H   ALTER TABLE ONLY public.historyfiles DROP CONSTRAINT hostoryfiles_pkey;
       public            postgres    false    228            D           2606    16469    userdetails userdetails_pkey 
   CONSTRAINT     _   ALTER TABLE ONLY public.userdetails
    ADD CONSTRAINT userdetails_pkey PRIMARY KEY (user_id);
 F   ALTER TABLE ONLY public.userdetails DROP CONSTRAINT userdetails_pkey;
       public            postgres    false    216            F           2606    16471 %   userdetails userdetails_user_name_key 
   CONSTRAINT     e   ALTER TABLE ONLY public.userdetails
    ADD CONSTRAINT userdetails_user_name_key UNIQUE (user_name);
 O   ALTER TABLE ONLY public.userdetails DROP CONSTRAINT userdetails_user_name_key;
       public            postgres    false    216            S           2606    16533 $   filedetails filedetails_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.filedetails
    ADD CONSTRAINT filedetails_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.userdetails(user_id);
 N   ALTER TABLE ONLY public.filedetails DROP CONSTRAINT filedetails_user_id_fkey;
       public          postgres    false    218    216    4676            U           2606    24672    filestable folder_id    FK CONSTRAINT     �   ALTER TABLE ONLY public.filestable
    ADD CONSTRAINT folder_id FOREIGN KEY (folder_id) REFERENCES public.foldertable(folder_id);
 >   ALTER TABLE ONLY public.filestable DROP CONSTRAINT folder_id;
       public          postgres    false    4682    222    220            X           2606    32922    historyfiles parent_id    FK CONSTRAINT     �   ALTER TABLE ONLY public.historyfiles
    ADD CONSTRAINT parent_id FOREIGN KEY (parent_id) REFERENCES public.historyfolders(f_id);
 @   ALTER TABLE ONLY public.historyfiles DROP CONSTRAINT parent_id;
       public          postgres    false    4688    228    226            T           2606    24653    foldertable user_id    FK CONSTRAINT     }   ALTER TABLE ONLY public.foldertable
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public.userdetails(user_id);
 =   ALTER TABLE ONLY public.foldertable DROP CONSTRAINT user_id;
       public          postgres    false    4676    220    216            V           2606    24682    foldermapper user_id    FK CONSTRAINT     ~   ALTER TABLE ONLY public.foldermapper
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public.userdetails(user_id);
 >   ALTER TABLE ONLY public.foldermapper DROP CONSTRAINT user_id;
       public          postgres    false    4676    223    216            W           2606    32927    historyfolders user_id    FK CONSTRAINT     �   ALTER TABLE ONLY public.historyfolders
    ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES public.userdetails(user_id) NOT VALID;
 @   ALTER TABLE ONLY public.historyfolders DROP CONSTRAINT user_id;
       public          postgres    false    216    4676    226            �   /	  x��[Y��6��f���K��#qq�*v�b�*����B�HNR�H\@���+h�]�����vv����Xm��rG���h[56Wg�s-;XA�e+;wk�����辐��'T�Om��^U��ʶV8I�9餹����n�a'{���sC�����'��ւZ�?�l�~+�p��5'��OѺRv9��[kl�ϴ����x���O�3\�ou0w��ғJ�
����y�ى���)W	�IC�H��핢_y�g��-
��b��Fm�.��4�]~\���>���^�k#�(n/�Ϋ�%�jY��Y��8�5G�
y(���!��i��x��Ulo�?PW��G��Sg!+u�M�T��/�e�z�!j�'<�w'� k�]+��4�t]��S�h��b�����Z9�tj���Ik��v��F8 ~�?~����	�\��xQ��z�be������ڌQ=4k&�3_)֫*��vg�zQ�bū�:��T����~�d]��d�����d[��p5�⹊t�����̈��.����i�pD���9�ѹ��s���!D8���4Wr����m�sșoEҴ�@��c�<���HJ<�K�c��Fxg�J��#*͝���D4�J��ˌ�L㺻֔Q�kj��F�2
�k�@�#ͱ6@IA;g��m� �3�Y݆^ZҠ�|p+��1��S�+,�g�dG(fΤ�hTאl�=��pd=��d�Ǌ
�6a- �5���`.���%3�ωP�Z�^Ү�.�N�C[X�"����#��T��N�}������?|�e_UG��m):軣#9K )ќ=?Su>���{)��/�&�*·������D�qQ꼖��lˬ%K��d� �$�����s��!����������Ixw$M�E�N��J�I�)�+��5�WJ�Z)�TPXH�!91���jn^�����avt�H�U�!��[OH�O�H���8�=�Ω��TpY������:��� uX���w~�ųZD�z&Z�b�ŔJX�孎�����U/ĆU)f8�?ډ���k�<h����$��/}c 5�H�(�suX5�z�3�ܽ<�%sE��>dB�1NZ��LFm����G*�^*O�Z�)c&��hX�֔Gk.ë�L̤��<��az�fH�L�0b�Q�#�g:b��]����6ڠ!�[��n�B��\�X7��!/ܪ6	CWq��e��3�匲-�Q7-�+]V����јզ�;�)��s����r���R��5� Z;i��Ƒ׻�:�r]a��3�g�F�лa�~�|�S�0�m��԰Z��P(v-�	Ƹ2Ȫ`d���)ݚ,�q_�0��;9'<7E2x�A���� �A�6y�<��ƠO�2�ZQ	I��.(ּkI�`��L@�=s)�+�׵4��:Xw�>*u���2z��ٻf2,�m	I���l�wq��=P��,��&�D�^ۮ:�Y<����:$�iU������:޳X'<ň���W����-C1z��Q,�P�Ϫ���4�S4���Jo˛{Գ���mE�F&�I�6^S�=1�fp��>��א>��j>� ʜ��O�ЇSm?f�/�:�>{>��T�
F��ޝCCI's�?䇹G�WH&ʂ듎}�9�.Wy�J���Yz�F
����n��.X�[r�a����(�ʍ��bL�F�u�F݈��8N�m��+�w�v:�4�>#$d�!�KH�!�K��-7��5�ie�C�ʿ�ut;��J=�T_����������P�^i�P»[:U�-ݕ�N fP���(�Ÿ�'���r���"�f���p��%��s�[�(n��gh���"��%��UC/�D��jd����{z��?N��蟻+���Ks���Ṱz���6�Kt�E��0c�x�]_�O���;)r+=�]���~e���{,u���}5��὇�V�@sMO�N@b��k�
U�Ա]ʫ��_̻�s'#�-2�p���'e=e�ֹ�2^K�=�FU0�l�xe���s�~�6�d֗K���~�z��K������
�?A��~v�,�F�~V^�z�,�������^��=Pv6}���Uo؃��Z�a�F�J�$�cGj�G���OY�4C\=�K⚷;Î]���u1_��P��8�K�4�YH[�2x�@C���4F�t���k��Ƌ�X�]�`��0쏾�m�+d����zr�z�|��`��I��)�ԩ��ذ���<���m�s��NJag����@i�9��n���|���8ؼ>�یҪ37�iω*��r�=�v�h52����l+�Ǜ��������mr�Gv�]^n_����a�+��������?��b      �      x��\K���?W>���{���cn�8ٿ��1�d,X�{�3m�t�v��q>�R��)����+JŗHk�nv������}�n�3�ѾQB�R��J�k��@m��a��zj�/?�e��	� -���6�iow�ȕ�� ��J�k-��5v����]�� V�J�ki��7��--E� B�q%ݵ�H�}w�;�J�/�����Z$�ͱ�۝���ƗW�J�k��4�9m���3}ݑ�KrZ��͛������W���=�_��>7N 0�@�B�+��E�jn7�����xKG���	�2�YLF7?�L����� HEW������(��&C��%D_	w�	b���8��5��g�*pt.�D�FQ#Bjk
D�BB��{x��ߵ��������R�V�da���6$���<���C�'��6��b�js޼ߜڛ�ph��]w�L�s���*�~y���$��z�[8��$N"���㟇v��sR��D
��GnU���্�,?u-�4��iU�д��/�$hƼkO�A@A�g*W�T֔���ݾ��(��d`�Z������U��|�����ɣ��x�Q��dAf<���!�����6&T�t���\�B�g�D�����W��]��4(����M�IF���|���}��x���j�m>���<��AF���@+C\u������~����/��\���q��ns�6g!^[����C��� 3��<���l�+,�v�9�����痦��VI��-�lU��.KcBX-�|U͚�[X:/K`�0���riN2�(TL.A\\�W�=!���jy5{1y��,%aJlRF�SÛ��Z�3_�1h!��)=\����lT�j=C��
���Y�z$#�ɉ]�aaW�b0��&���°��L#>�QX��3�R�Ym1n��LK�bW��Զ��ql�7�m�!^�{�jl�Q��"{��9
�9��Li��(��(_5aB-0 ��q�is<o^�����qt/ӟ��cZ�2`����r=}�7O�\e���F��(V�ata!�6�MCh����؞�o�¨���A(D*�_�*)��C���zqu�=~��w)���ãI)�u�F��Y^�e�6�<��gA����T�~RO��YH3A�g!�鞅t��O��Y�0A�g!G-�f��9�2������.�h%�fL2_H�����2#T$+RG��n��7>Ys'f�(t�@mrv��%$Mj�P�G��v̙`����9������ڔ��C��eU�βٱ* �a^};��(4� �Ӫ�L�fA�R���(�S�iU ��*`�c��r�xJp�˪���CU �5WX�U�7LU�Xz�
 ��$���b�[W&��h�3�����%� 	lro%F�Vx�UCF>��s�{pPb=�w3�5ր�k��
N�$��0�XRM�	$`���~�v�j�����6�� gP�ʮ%�%?J��j�=D��c��^@�jr?g:&�����9zL�ג{����æ��~!�!�jYO��!�ZI�bp�i>��Q0{"y�D�YM��< �{ ڋ��b�C `w1�_��h�~%�/�D�n�%^MH���s_I�}JҪugs�L��r-��s5�br�+�=�ե��W�{�j>��#�t�91f5��������k�2�q��{_I��!����ާ���}%;����Xa*ū�`�+ɽ�$�>����W�{��Zro��8����U[� p����8{�טM��kyk4K����͑e�Q������+ɽ����!�����W�{�����W�{�ɋɽ�$� V�����m���B
&1%v���Z��]o��� 5!��`ە�\�;��4���=��ow�-~ �s�Y��|�k��v�y���-�ةϛK�vy�c�9�5���v�p�ϰ`�D��(�������q��T�"L�0�:�@6�u@lH����l?����w�EBԇ��a��mf��F��=D�7������q�!��b��2>��7����p�uI�9���l����f��}iϧ���b3J�4݌Mq����?Q*�2�Yb� �I���9�2JEFtd�I澹m����dy�!��$�/`\�pC�dו�x�}d�9s����\3�vsL�ν�n�\��x�2�'ODߟ+����*���9��,���b��ٜ������Ә���e��x��Ү�S���Q��kZ��O�!��8����2e�i���iMD(�W.)��@�\�D	Ŋ!ފH%0ߎ�=%�H7��!�YQ!!X=:�:!չ@О�����$B��@��"!�nݪx"���5�D蛟�m���^uO����q�9Q��^�Bs��)*�����r�j�+i�p��bc6S����3���%ҫi:lP�,�Bj�~�liI^Z�_�!���d�IH["����#�@�������1��GK+�'j21�*��Έk�: ��������2)bsh���H�������k#����s�a��@��	��߿�vIP?��m����)>�p�$�x�#Y�x�;�R����G�V��P�`�;"OB��FWJ;�IF3W`t�	���۞m��]v��iq���@���{]:ˈ
�A@��c��wÍ�D��AC .x��H�C���*��~�<�� P?<}�0�s�0k$�U���XM!�T�9��A�y:�4�[<~oOO��E�TTћ7�EQ{ N�r��G;B+%�:�B��.�W�hS�G@�1�c�xٔk$�����@M���>5r9�˪�#l ��;߿���R�I�F؛���ؘm�s��(p�ܭ��G�{�';㪃�^�r��Y�%%%���l���u>y�C�7,�t�������;�Dқ�ͩ=��t8���^P�T���3c&��x�z5��ML�=���n�w�O�4�����м�z:�`���y���ȂW�o��HK$9ǹ�8�M�K�ܞ����1ᔠk��/��b ޓS3'���ze)h�S %Ϙ�H�����t���5U�9��[6Ee�eK�L["gl���{���u�!h�}s6����W��r��-�d�!�i�����v(lJjs)H]���Dp���R�*;:Ҹ`D�^�
|���Id6}6�~B����}:&ǜ*^�;�ԚWG���#�)Ry7�!��> �8��J���� �:�69Jve3I�Ϩt=p�L�jZ�a�y��s;�h~�] U�Ť[p��\�q̩�1:�_~ލ�JFE���.�@��C�\+��r��
h�w�ׇ�p�=/u����I�y���ͫ��q���Kw��񱱖��b�P2�0����� ��6��1k4L���s�χݫI�� '�ρ�n>vX.���(=ʛ]�;�.|=Ĺ�bXz��>��y���wxpU���F	9]�)�<u��������y�ke�j��RQZ�&�{z�C����R�_yUoa�?�y=XV͜�tu�@�7#��-@��<�p(s�/�?�������v�Q�p��bN`���7��z�N�2y������<�u���s����3��#����V�:� xdNz����o2~fԴta��rPM�w?@�r��N��:�%hb����L�w�σ���*3�B��b�BΌ��������Mb�r�l�����+-�j�nN�#d!KX�|3����P��!����L�V��nLdCJd�R/ݱ��8�9�qD��M��+�Ҙ�G���ғ���
��ә���9��)�<n�H�s���2�
��"b�k,5�M�����Ì�js<S <�b���&�o�tu�_�����#� ���B.0�R����ַ���=|J��4E�6^)��)�o���vx#���p��x������J�I��[��E��qII�q/�_�/AJ��ػi!�)�$���m���폠	��v�N�]�/���J��.6mJ,=�*���x²��ڗ�YA	l�,�%n^Dl̏�<��lpl{1��Fb9Ǖ�"T�}/2�ڬ�0�L#XUd�M����c�#�f���ڿ����}�	*��p���6((��mE�C�ޔx3�
F߅ %�.u!� �  b�VR벒�bB(���1���aMcX6ԊH5'��ԞS6��_wh���y'��何��mE�m�'ݳ(%'m�}=G�?��=�2���}�J���ԣkQ���*E�gz��&�,�yO^V);kN�W�9����T� ���{��qO�&��˕P둡�R�o��n�IyJ�Q�(� ʹAL(Jdn�PZ�6P,�c�H��@���Jk��bA>6P(m�avo'pk��t3n��b�ı������C�6! .(J�YOi�	b�@l�>|ARc�g�����
e$�@��K1�#B�4P�Z��m,(Jjկ��(���[6P�g����|Eyc�2a�@��07P(����_Y1k�`	�VX9k�X!T�
�M�ճ�B3k�`	�!X;k�X!t����Y�
�b�@Q��
.��@QG9Qk�(t��P���b��������ꢁ��F=q��@�9�� �b�@��t�+@_6P�-�eEirD�\E��\�V���vD(8��ߢ\M�H��6P�K�
��ol�`���'R6PpR����,(�FRK��bA?q�XU(J%�ʇjE���@�|�7Pp�!C.7C�07C�6�J��$��A]j�(����eE��Aj��@&���%[A�p��b�V(X ��\@M�_j+����oD��@Q�#��++U��b��
��b��+���(�]�@�%&�J�R�q�@�Bj�#»�6�i�d��"&�6Pp<�nk�o�@QM8�Pk�wp��M�G�#f�¥�9a��I\��q����)�K/�25�h1��x&�#y����x�Z��#u��˩q���j�Y��ޢ٩�L�F��q�L��R�b��q��w�Ɨ���og�����m;n4O�k~j<ӛ���Wjvj�Xz?5��S��L�)$W8aH a��7����8A�x��Ը�M�Si2���� \ej<#�ʄ��45q4D���Ol�����ũq�NU3O��S����9O�>5�Y�)K��z}j�䗧��ũ�e?�/M�g���̝��L��<�3��S��S��(Qw�:_�����v�oְ��c��	-��b�s�B@UΫ�	3�G���:��Qzf��h�2��E:a�{���y��$�x�xڭΫg�>y�^�2Gq^=�R���E.Ϋg��j<�qp�6�^޲<��A3�C�C���E��k#/�GW3FF�Ϋ� ��yuϯ)�*��/̫gƌ��P~^=3e��W��	8uFy^]���ڼzf�fj�:a¥y��S1���v�a^=��Ԛэ��j��y*W3gV~�{)�<��!�b�����_�gVs��y�s�F��ʨ|�1&��ʨ|�Ʀ��4��Y�G�~}�~D.�!���Q�R�z�f#?*_�,N\�/U���N^�/Y�Qy{�8*_�'��sF�ϗ~p#}�yd}T~�M�:'g/>RX��'>R�������q<�      �   *   x�36�4331�2Ѧ�P�J�Bh���1��4������ :�	�      �   �  x��VMo9=+�b��;�(���� �C��.P`ы�̦N�L�	��_J����͖�=Q��#�,�ߊ]��?l��ͫBV_�k�&TF��h(����#�����U�ܖ*Xͦ�B���^=���6����B�?(���z�>�k!�=����Q��21
�U{ܭ�eG�e~��������]κ�Bg-.����m{8���z�8X�E0����(kr����6X���0�>���DK���R�`�=k�3�����u��m���p������T"���u{��֏�#������޼���q�������mԍK�<�s"\���@}��� �IZ�����!�g�����Y@�F�f��� z, T�KZ��sx1f�O~��$X���V�괧��!3,��s�8����Y�8��������";,�U�QA�E*������z�<tu��FsPa�>���9��Et�.��L�E�`O�A�Y�e��$� �t"��~�d�O;1� (Jci�#���3�ò2�+6fIլ��;�0��lH��օ>
���T�����y:n_^��O�,�oD_�S�pLlj<�I�%����,Ʉ�y�욝�U����Pwln&������e��]�Q����I�Q8[�H��6�h	H֌�U`#�b�B���5z�����������.�$6͑2�e��&�^`��4�^7�&Y 8Fy�S����i�7ŗ����q���Z�xQ$�1��$#�Ø?oԇ���y�5#,��R�,Lm
�+��17��W_nd�f�<�/��w3��q�����_���N�y�=�G��g�couGi�â�V�\�A�2a#[6rm�Q�&ͨ� �_Lɺi��tMN�^F��5�i:'m���0Þ'�+v��fS����x�b#���$�1��|0���HBc��+�#`lnKT��b�f�30u����3�~�����c�      �   �  x�}�Mn�6���)|�V�'�Z�H q�x��lh�i�G-)"����S$EO,�l/���X���'So��G+JD�i�j^�����y��?WW�uu�(���Jr�@���z��0��t��ôIe�R.ڲt���y�nj�B�����ߛ�A�h��5��:8��m�y�D�0֒���u�h���7�������(g�����4HeK���~k�� �LC�Q��䢘����s_�"�,Ү��a�Iڤs%��[�'��o��r�����)*Ixyڽq��Y��L6	��j�U~��
+:�l�k�;����o��4����.H#�-ѲJ��^�4 �7�z?��[���C�O�� ��@1����|�p�x�x� +�1�<g?��d;?�SE,G�����0�]�aC��;#�sX�oÒ! I�E)Xq�?Y_����Yp�bA��t�BV ��Y��7�6p�"+�0��\8I�����C�YHЀ�yZ./�蒖"�%d����!�P d���̡t[f~�xh�}���/ߩd�e��c{Mv�d2�Bf~g��o�y?�h�bK�_z�퇮��H�H�tz����cz��6�� �^0��M��E�"P^�:
�>�ф� ����=(D�.2��0�p!2�cA�)B�I�� �W����!SM����ˆL�X�%�^�oT4����!�:���w�7���6%
��bc�X�܈RBdq2��į2"3�)�E�uHd(����M�S"c�"R ���DƢG���J9�Q���Q>���ν}�G(�<W)W2�ຆ�o�%��
f)U6TFw4R"�p2_��M����`��@I~Of��2ᒫ��HV��<	�[�7�J���2;\��g<V�Zl� �Ԉ������J�YW      �     x��ҽN�0���y�4���
�%K���%(�Q�R���0�7���;��-<���P�]����]�0�.����w�p�E��NG߃��
��2�h���M���m�m\��阿���~��JK�!�����M{Nq+AZe*�.a��)˽�M��#u�I�ۣ�k�;��FԉR<���x�H�߀�jQC)�t�%0�����y�r���8��������C�m�4��6�5�cWt���Ғ���ɔ�VQ)��?�l�c�}���      �   .   x�36�46�4202�54�50W04�24�2��36557������ ��      �   �  x����q[1�5_1|	p��͆���Q����CI�@��Ë��.���ǿ��^�{T�P�*��]�&Tkʕ8|��Ѷ���O��rh�ޮ���!�2��^�P��8S�%
�X�"�ښ�sz��!P[�@�;��=���@*oB�j�nuj���*�iF@�l�;�7�mJ�i����I{��^��P�|�j�s]?w��XGzF�.�D��t�ED< �^R�h�[AȬ����A�v�^n�3(}�*8$a5Wl���2&0W�hup����Z��L�m���� ��s��Y�����;�������uߙ�Z���$���Z��Q����I^>n��z�O_�`���ΰ���gPR��=�D'H�!�	�<#�?�	R{����2�d�X��j8���Ω&�g��*��&- �Ҝc����o�q�����     